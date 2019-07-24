package org.vitrivr.cineast.core.data.providers;

import java.util.Optional;

import org.vitrivr.cineast.core.data.Location;

public interface LocationProvider {
  default Optional<Location> getLocation() {
    return Optional.empty();
  }
}
