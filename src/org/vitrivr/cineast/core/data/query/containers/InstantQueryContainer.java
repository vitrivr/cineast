package org.vitrivr.cineast.core.data.query.containers;

import java.time.Instant;
import java.util.Optional;

import com.google.common.base.MoreObjects;

public class InstantQueryContainer extends QueryContainer {
  private final Instant instant;

  private InstantQueryContainer(Instant instant) {
    this.instant = instant;
  }

  public static InstantQueryContainer of(Instant instant) {
    return new InstantQueryContainer(instant);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("instant", getInstant()).toString();
  }

  @Override
  public Optional<Instant> getInstant() {
    return Optional.of(this.instant);
  }

}
