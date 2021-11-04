package org.vitrivr.cineast.core.data.query.containers;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.vitrivr.cineast.core.data.Location;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.core.util.web.DataURLParser;

public class ParameterisedLocationQueryTermContainer extends AbstractQueryTermContainer {

  /**
   * The {@link Location} object contained in this {@link LocationQueryTermContainer}.
   */
  private final Location location;

  private final String parameter;

  /**
   * Constructs an {@link LocationQueryTermContainer} from JSON data.
   *
   * @param json The JSON data that should be converted.
   */
  public ParameterisedLocationQueryTermContainer(String json) {
    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
    String converted = json;
    if(json != null && json.startsWith("data")){
      converted = DataURLParser.dataURLtoString(json, "application/json");
    }
    final ParameterisedLocation ploc = jsonProvider.toObject(converted,
        ParameterisedLocation.class);
    if (ploc != null) {
      this.location = Location.of(ploc.geoPoint.latitude, ploc.geoPoint.longitude);
      this.parameter = ploc.parameter;
    } else {
      throw new IllegalArgumentException("Failed to parse the provided JSON data.");
    }
  }

  /**
   * Constructs an {@link LocationQueryTermContainer} from a {@link Location} object.
   *
   * @param location The JSON data that should be converted.
   */
  public ParameterisedLocationQueryTermContainer(Location location) {
    this.location = location;
    this.parameter = null;
  }

  public static LocationQueryTermContainer of(Location location) {
    return new LocationQueryTermContainer(location);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("location", getLocation()).toString();
  }

  @Override
  public Optional<Location> getLocation() {
    return Optional.of(location);
  }

  public Optional<String> getParameter() {
    return Optional.of(parameter);
  }


  public static class ParameterisedLocation {

    public GeoPoint geoPoint;
    public String parameter;
    public ParameterisedLocation() {
      // Empty constructor for de-/seralisation purposes
    }
  }

  public static class GeoPoint {

    public float latitude;
    public float longitude;
    public GeoPoint() {
      // Empty constructor for de-/seralisation purposes
    }
  }
}
