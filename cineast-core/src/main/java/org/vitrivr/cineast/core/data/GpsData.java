package org.vitrivr.cineast.core.data;

import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MetadataUtil;
import org.vitrivr.cineast.core.util.OptionalUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Container for GPS data, i.e. location and datetime. The data is extracted from either the Exif
 * data, if available, or from a complementary JSON file. See {@link #of(Path)} for more
 * information.
 */
public class GpsData {

  public static final String KEY_LOCATION = "location";
  public static final String KEY_LATITUDE = "latitude";
  public static final String KEY_LONGITUDE = "longitude";
  public static final String KEY_DATETIME = "datetime";
  public static final String KEY_BEARING = "bearing";
  public static final String KEY_DATING = "Dating";

  private static final GpsData EMPTY = new GpsData(null, null);

  private static final Logger logger = LogManager.getLogger();

  private final @Nullable
  Location location;
  private final @Nullable
  Instant time;

  private GpsData(@Nullable Location location, @Nullable Instant time) {
    this.location = location;
    this.time = time;
  }

  /**
   * Extracts the GPS data from a given file using Exif. If the Exif data is incomplete, additional
   * data is retrieved from the complementary JSON file named after the original file, e.g. {@code
   * image_0001.json} for {@code image_0001.jpg}.
   *
   * @param file file to extract data from
   * @return an object containing the extracted information, if available, otherwise an empty
   * object.
   * @see #ofExif(Path)
   * @see #ofJson(Path)
   */
  public static GpsData of(Path file) {
    return ofExif(file).orElse(() -> ofJson(file));
  }

  /**
   * Extracts the GPS data from the given file by reading the available Exif data. In particular,
   * latitude, longitude, date and time stamp are read from the file.
   *
   * @param file file to extract exif data from
   * @return an object containing the extracted information, if available, otherwise an empty
   * object.
   */
  public static GpsData ofExif(Path file) {
    GpsDirectory gps = MetadataUtil.getMetadataDirectoryOfType(file, GpsDirectory.class);
    if (gps == null) {
      return ofData(null, null);
    }
    Location location = Location.of(gps.getGeoLocation());
    Instant instant = gps.getGpsDate().toInstant();
    return ofData(location, instant);
  }

  /**
   * Extracts the GPS data from the complementary JSON file named after the original file, e.g.
   * {@code image_0001.json} for {@code image_0001.jpg}. The method expects a top-level JSON object
   * in the form of
   *
   * <pre>
   * {
   *   ...
   *   "location": { "latitude": 47.559601, "longitude": 7.588576 },
   *   "datetime":  "2017-05-03T08:37:47Z"
   *   ...
   * }</pre>
   *
   * <p>Location information requires the key {@code "location"} containing an object with numbers
   * for {@code "latitude"} and {@code "longitude"}. Alternatively, the coordinates can also be
   * represented as an two-valued array with latitude first, longitude second, such as {@code
   * [47.559601, 7.588576]}.
   *
   *  Time information must either be a valid ISO 8601 instant description, with key "{@code datetime}" or
   *  conform to the description of {@link #parseFuzzyDating(String)}.
   *
   * @param file file to extract json data from
   * @return an object containing the extracted information, if available, otherwise an empty
   * object.
   */
  public static GpsData ofJson(Path file) {
    Optional<JsonNode> root = MetadataUtil.getJsonMetadata(file);
  
    Optional<Instant> instant;
  
    if(root.isPresent() && root.get().has(KEY_DATETIME)){
      instant = root
          .map(o -> o.get(KEY_DATETIME))
          .map(JsonNode::textValue)
          .flatMap(GpsData::parseInstant);
    }else if(root.isPresent() && root.get().hasNonNull(KEY_DATING)){
      instant = parseFuzzyDating(root.get().get(KEY_DATING).asText() );
    }else{
      instant = Optional.empty();
    }

    Optional<Location> location = root
        .map(o -> o.get(KEY_LOCATION))
        .flatMap(GpsData::parseLocationFromJson);

    return ofData(location.orElse(null), instant.orElse(null));
  }
  
  /**
   * Parses fuzzy Dating specifiers. Several input formats are accepted:
   *
   * <ul>
   *   <li>US-style month / year: {@code [M]M/YYYY}</li>
   *   <li>German annual estimate: {@code (vor | ca. | nach} YYYY</li>
   *   <li>Latin annual estimate: {@code (pre | circa | post) YYYY}</li>
   *   <li>English annual estimate: {@code (before | approx. | after) YYYY}</li>
   * </ul>
   * The <i>annual estimates</i> are respected with best effort.
   *
   * If the Dating given does not contain information for
   * <ul>
   *   <li>the day of the month, the 15th is then used </li>
   *   <li>the month, the 6th (June) is used instead</li>
   *   <li>the hours of the day, noon is used</li>
   *   <li>the minutes or seconds, 0 is used for this</li>
   * </ul>
   * Following this approach, the date is somewhat 'centered' within a year, so that the {@link org.vitrivr.cineast.core.features.TemporalDistance} works best.
   *
   * @param dating
   * @return
   */
  public static Optional<Instant> parseFuzzyDating(String dating){
    if(dating == null){
      return Optional.empty();
    }else if(dating.isEmpty()){
      return Optional.empty();
    }
    logger.debug("Parsing dating: "+dating);
    String[] prePrefixes = new String[]{"pre","vor","before"};
    String[] approxPrefixes = new String[]{"ca.", "ca", "circa", "approx.", "approx", "approximately", "about", "around"};
    String[] postPrefixes = new String[]{"post", "nach", "after"};
    
    int year=-1, month=6, day=15, hours=12, minutes=0, seconds=0;
    if(dating.contains("/")){
      /*
       * US-style M/YYYY dating specification
       */
      String[] splits = dating.split("/");
      if(splits.length >= 2){
        try {
          month = Integer.parseInt(splits[0]);
          if(month < 1 || month > 12){
            throw new NumberFormatException("Month out of bounds: "+month);
          }
        }catch(NumberFormatException e){
          logger.error("Couldn't parse month value "+splits[0]);
          logger.catching(e);
          month = 6;
        }
        try{
          year = Integer.parseInt(splits[1]);
          if(year < 0){
            throw new NumberFormatException("Year cannot be negative "+year);
          }
        }catch(NumberFormatException e){
          logger.error("Couldn't parse year value: "+splits[1]);
          logger.catching(e);
          year = -1;
        }
      }
    }else{
      /*
       * Annual estimate, as described above
       */
      String[] splits = dating.split(" ");
      if(splits.length >= 2){
        int modifier = 0;
        if(Arrays.asList(prePrefixes).contains(splits[0])){
          modifier = -1;
        }else if(Arrays.asList(approxPrefixes).contains(splits[0])){
          modifier = 0;
        }else if(Arrays.asList(postPrefixes).contains(splits[0])){
          modifier = 1;
        }else{
          logger.error("Unknown Dating prefix: "+splits[0]);
        }
        try{
          int _year = Integer.parseInt(splits[1].trim());
          year = modifier + _year;
        }catch(NumberFormatException e){
          logger.error("Year not parsable: "+splits[1]);
          logger.catching(e);
        }
      }
    }
    if(year!=-1){
      String instant = String.format("%d-%02d-%02dT%02d:%02d:%02d.00Z",year,month,day,hours,minutes,seconds);
      logger.debug("Fuzzy instant pre-parsing: {}", instant);
      return parseInstant(instant);
    }else{
      return Optional.empty();
    }
  }

  /**
   * Obtains an {@link Instant} of a given {@code String} such as {@code 2007-12-03T10:15:30.00Z}.
   *
   * <p>The string must represent a valid instant in UTC and is parsed using {@link Instant#parse}.
   *
   * @param string string to parse
   * @return an {@link Optional} containing a {@code Instant}, if the given text contains a valid
   * instant, otherwise an empty {@code Optional}.
   */
  public static Optional<Instant> parseInstant(String string) {
    try {
      return Optional.of(Instant.parse(string));
    } catch (DateTimeParseException e) {
      logger.error("Could not parse {} as Instant: {}", string, LogHelper.getStackTrace(e));
      return Optional.empty();
    }
  }

  /**
   * Extracts location information of the given {@link JsonNode} from the {@code latitude} and
   * {@code longitude} fields. If not available or valid, the coordinates are extracted as an
   * two-valued array with latitude first, longitude second. For example, the JsonNode may look like
   * the following:
   *
   * <pre>{"latitude": 42.43, "longitude": 7.6}</pre>
   * or
   * <pre>[42.43, 7.6]</pre>
   *
   * @param node json node to extract location information from
   * @return an {@link Optional} with a {@link Location}, if both "latitude" and "longitude"
   * coordinates are available and valid, otherwise an empty {@code Optional}.
   */
  public static Optional<Location> parseLocationFromJson(JsonNode node) {
    return OptionalUtil.or(
        getLocationFromJsonObject(node),
        () -> getLocationFromJsonArray(node)
    );
  }

  private static Optional<Location> getLocationFromJsonObject(JsonNode node) {
    return getLocationFromFields(node.get(KEY_LATITUDE), node.get(KEY_LONGITUDE));
  }

  private static Optional<Location> getLocationFromJsonArray(JsonNode node) {
    return getLocationFromFields(node.get(0), node.get(1));
  }

  private static Optional<Location> getLocationFromFields(@Nullable JsonNode latNode,
      @Nullable JsonNode lngNode) {
    return OptionalUtil
        .and(nodeAsFloat(latNode), () -> nodeAsFloat(lngNode))
        .map(p -> Location.of(p.first, p.second));
  }

  private static Optional<Float> nodeAsFloat(@Nullable JsonNode fieldNode) {
    return Optional.ofNullable(fieldNode)
        .map(JsonNode::numberValue)
        .map(Number::floatValue);
  }

  private static GpsData ofData(@Nullable Location location, @Nullable Instant time) {
    if (location == null && time == null) {
      return EMPTY;
    } else {
      return new GpsData(location, time);
    }
  }

  public Optional<Location> location() {
    return Optional.ofNullable(location);
  }

  public Optional<Instant> time() {
    return Optional.ofNullable(time);
  }

  /**
   * Returns this if all values are present, otherwise invoke {@code other} and set all undefined
   * values from the result of that invocation.
   *
   * @param other a {@code Supplier} whose result is used to set undefined values
   * @return this if all values are present, otherwise a {@code GpsData} whose data is set by this
   * or alternatively by the results of {@code other.get()}
   * @throws NullPointerException if {@code other} or the result of {@code other} is null
   */
  public GpsData orElse(Supplier<GpsData> other) {
    Preconditions.checkNotNull(other, "Supplier cannot be null");
    if (location != null && time != null) {
      return this;
    }

    GpsData that = Preconditions.checkNotNull(other.get(), "Value of supplier cannot be null if this is empty");
    Location l = this.location != null ? this.location : that.location;
    Instant i = this.time != null ? this.time : that.time;
    return GpsData.ofData(l, i);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("location", this.location)
        .add("time", this.time)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GpsData gpsData = (GpsData) o;
    return Objects.equals(location, gpsData.location)
        && Objects.equals(time, gpsData.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, time);
  }
}
