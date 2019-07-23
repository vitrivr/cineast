package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.SemanticMap;

import java.util.Optional;

public interface SemanticMapProvider {

    default Optional<SemanticMap> getSemanticMap(){
        return Optional.empty();
    }

}
