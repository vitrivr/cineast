package org.vitrivr.cineast.core.data.query.containers;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.vitrivr.cineast.core.data.Location;

public class LocationQueryContainer implements QueryContainer {
  private final Location location;
  private float weight = 1f;

  private LocationQueryContainer(Location location) {
    this.location = location;
  }

  public static LocationQueryContainer of(Location location) {
    return new LocationQueryContainer(location);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", getLocation())
        .toString();
  }

  @Override
  public Optional<Location> getLocation() {
    return Optional.of(location);
  }

  @Override
  public float getWeight() {
    return this.weight;
  }

  @Override
  public void setWeight(float weight) {
    this.weight = weight;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getSuperId() {
    return null;
  }

  @Override
  public void setId(String id) {
    // Ignore
  }

  @Override
  public void setSuperId(String id) {
    // Ignore
  }
}
