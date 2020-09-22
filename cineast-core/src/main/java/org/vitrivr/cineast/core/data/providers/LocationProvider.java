package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.Location;

import java.util.Optional;

public interface LocationProvider {
  default Optional<Location> getLocation() {
    return Optional.empty();
  }
}
