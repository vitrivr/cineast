package org.vitrivr.cineast.core.data.providers;

import java.util.Optional;
import org.vitrivr.cineast.core.data.SemanticMap;

public interface SemanticMapProvider {

  default Optional<SemanticMap> getSemanticMap() {
    return Optional.empty();
  }

}
