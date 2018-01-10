package org.vitrivr.cineast.core.data.distance;

import java.util.Objects;

abstract class AbstractDistanceElement implements DistanceElement {
  private final String id;
  private final double distance;

  protected AbstractDistanceElement(String id, double distance) {
    this.id = id;
    this.distance = distance;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public double getDistance() {
    return this.distance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractDistanceElement that = (AbstractDistanceElement) o;
    return Double.compare(that.distance, distance) == 0 &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, distance);
  }

  @Override
  public String toString() {
    return "AbstractDistanceElement{" +
        "id='" + id + '\'' +
        ", distance=" + distance +
        '}';
  }
}
