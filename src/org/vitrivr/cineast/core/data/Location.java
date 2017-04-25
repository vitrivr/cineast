package org.vitrivr.cineast.core.data;

import static com.google.common.base.Preconditions.*;

import com.drew.lang.GeoLocation;
import java.util.Objects;

public class Location implements ReadableFloatVector {
  private static final int ELEMENT_COUNT = 2;

  private final float latitude;
  private final float longitude;

  private Location(float latitude, float longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public static Location of(float latitude, float longitude) {
    return new Location(latitude, longitude);
  }

  public static Location of(GeoLocation geoLocation) {
    return of((float) geoLocation.getLatitude(), (float) geoLocation.getLongitude());
  }

  public static Location ofFloatArray(float[] array) {
    checkArgument(array.length == ELEMENT_COUNT,
        "Given float array must contain %s elements, but found %s.", ELEMENT_COUNT, array.length);
    return of(array[0], array[1]);
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }

  public String toString() {
    return "(" + this.getLatitude() + ", " + this.getLongitude() + ")";
  }

  /* ReadableFloatVector */
  @Override
  public int getElementCount() {
    return ELEMENT_COUNT;
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0: return this.getLatitude();
      case 1: return this.getLongitude();
      default: throw new IndexOutOfBoundsException(num + " >= " + this.getElementCount());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location location = (Location) o;
    return Float.compare(location.latitude, latitude) == 0 &&
        Float.compare(location.longitude, longitude) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }
}
