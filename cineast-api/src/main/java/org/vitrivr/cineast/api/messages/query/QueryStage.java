package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A {@link QueryStage} contains a list of {@link QueryTerm}s. This object represents a stage in a {@link StagedSimilarityQuery}.
 */
public record QueryStage(@JsonProperty(required = true) List<QueryTerm> terms) {

}
