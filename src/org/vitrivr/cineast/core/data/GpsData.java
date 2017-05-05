package org.vitrivr.cineast.core.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MetadataUtil;
import org.vitrivr.cineast.core.util.OptionalUtil;

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

  private static final GpsData EMPTY = new GpsData(null, null);

  private static final Logger logger = LogManager.getLogger();

  private final @Nullable Location location;
  private final @Nullable Instant time;

  private GpsData(@Nullable Location location, @Nullable Instant time) {
    this.location = location;
    this.time = time;
  }

  /**
   * Extracts the GPS data from a given file using Exif. If the Exif data is incomplete, additional
   * data is retrieved from the complementary JSON file named after the original file, e.g.
   * {@code image_0001.json} for {@code image_0001.jpg}.
   *
   * @param file file to extract data from
   * @return an object containing the extracted information, if available, otherwise an
   *         empty object.
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
   * @return an object containing the extracted information, if available, otherwise an
   *         empty object.
   */
  public static GpsData ofExif(Path file) {
    Optional<GpsDirectory> gps = MetadataUtil.getMetadataDirectoryOfType(file, GpsDirectory.class);
    Optional<Location> location = gps.map(GpsDirectory::getGeoLocation).map(Location::of);
    Optional<Instant> instant = gps.map(GpsDirectory::getGpsDate).map(Date::toInstant);
    return ofData(location.orElse(null), instant.orElse(null));
  }

  /**
   * Extracts the GPS data from the complementary JSON file named after the original file, e.g.
   * {@code image_0001.json} for {@code image_0001.jpg}. The method expects a top-level JSON object
   * in the form of
   *
   * <pre>
   *   {
   *     ...
   *     "location": { "latitude": 47.559601, "longitude": 7.588576 },
   *     "datetime":  "2017-05-03T08:37:47Z"
   *     ...
   *   }</pre>
   *
   * <p>Location information requires the key {@code "location"} containing an object with numbers
   * for {@code "latitude"} and {@code "longitude"}. Time information requires an ISO 8601 string
   * describing the instant for key {@code "datetime"}.
   *
   * @param file file to extract json data from
   * @return an object containing the extracted information, if available, otherwise an
   *         empty object.
   */
  public static GpsData ofJson(Path file) {
    Optional<JsonNode> root = MetadataUtil.getJsonMetadata(file);

    // Check whether JsonNode is an object is omitted because JsonNode.get returns null otherwise
    Optional<JsonNode> locationNode = root.map(o -> o.get(KEY_LOCATION));
    Optional<Float> lat = locationNode.flatMap(n -> getFloatFromJsonNode(n, KEY_LATITUDE));
    Optional<Float> lng = locationNode.flatMap(n -> getFloatFromJsonNode(n, KEY_LONGITUDE));
    Optional<Location> location = OptionalUtil
        .and(lat, () -> lng)
        .map(p -> Location.of(p.first, p.second));

    Optional<Instant> instant = root
        .map(o -> o.get(KEY_DATETIME))
        .map(JsonNode::textValue)
        .map(string -> {
          try {
            return Instant.parse(string);
          } catch (DateTimeParseException e) {
            logger.error("Could not parse {} as Instant: {}", string, LogHelper.getStackTrace(e));
            return null;
          }
        });

    return ofData(location.orElse(null), instant.orElse(null));
  }

  private static GpsData ofData(@Nullable Location location, @Nullable Instant time) {
    if (location == null && time == null) {
      return EMPTY;
    } else {
      return new GpsData(location, time);
    }
  }

  private static Optional<Float> getFloatFromJsonNode(JsonNode node, String key) {
    return Optional.of(node)
        .map(o -> o.get(key))
        .map(JsonNode::numberValue)
        .map(Number::floatValue);
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
   *         or alternatively by the results of {@code other.get()}
   * @throws NullPointerException if {@code other} or the result of {@code other} is null
   */
  public GpsData orElse(Supplier<GpsData> other) {
    checkNotNull(other, "Supplier cannot be null");
    if (location != null && time != null) {
      return this;
    }

    GpsData that = checkNotNull(other.get(), "Value of supplier cannot be null if this is empty");
    Location l = this.location != null ? this.location : that.location;
    Instant i = this.time != null ? this.time : that.time;
    return GpsData.ofData(l, i);
  }

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
