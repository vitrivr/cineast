package org.vitrivr.cineast.core.data.query.containers;

import java.util.Optional;

import org.vitrivr.cineast.core.data.Location;

import com.google.common.base.MoreObjects;

public class LocationQueryContainer extends QueryContainer {
  private final Location location;

  private LocationQueryContainer(Location location) {
    this.location = location;
  }

  public static LocationQueryContainer of(Location location) {
    return new LocationQueryContainer(location);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("location", getLocation()).toString();
  }

  @Override
  public Optional<Location> getLocation() {
    return Optional.of(location);
  }

}
