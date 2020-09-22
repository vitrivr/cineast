package org.vitrivr.cineast.core.data.providers;

import java.time.Instant;
import java.util.Optional;

public interface InstantProvider {
  default Optional<Instant> getInstant() {
    return Optional.empty();
  }
}
