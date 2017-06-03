package org.vitrivr.cineast.core.data;

import java.time.Instant;

public class InstantVector implements ReadableFloatVector {
  private static final int ELEMENT_COUNT = 1;

  private final Instant instant;

  private InstantVector(Instant instant) {
    this.instant = instant;
  }

  public static InstantVector of(Instant instant) {
    return new InstantVector(instant);
  }

  public Instant getInstant() {
    return instant;
  }

  @Override
  public int getElementCount() {
    return ELEMENT_COUNT;
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0: return instant.getEpochSecond();
      default: throw new IndexOutOfBoundsException(num + " >= " + this.getElementCount());
    }
  }
}
