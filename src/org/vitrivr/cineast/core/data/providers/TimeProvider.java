package org.vitrivr.cineast.core.data.providers;

import java.time.Instant;
import java.util.Optional;

public interface TimeProvider {
  default Optional<Instant> getTime() {
    return Optional.empty();
  }
}
