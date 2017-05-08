package org.vitrivr.cineast.core.data.query.containers;

import com.google.common.base.MoreObjects;
import java.time.Instant;
import java.util.Optional;

public class InstantQueryContainer implements QueryContainer {
  private final Instant instant;
  private float weight = 1f;

  private InstantQueryContainer(Instant instant) {
    this.instant = instant;
  }

  public static InstantQueryContainer of(Instant instant) {
    return new InstantQueryContainer(instant);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("instant", getInstant())
        .toString();
  }

  @Override
  public Optional<Instant> getInstant() {
    return Optional.of(this.instant);
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
