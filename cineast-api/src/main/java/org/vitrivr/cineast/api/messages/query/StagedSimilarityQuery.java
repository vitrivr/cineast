package org.vitrivr.cineast.api.messages.query;

import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;

/**
 * A {@link StagedSimilarityQuery} contains a list of {@link QueryStage}s. Each of them is used as a filter for the next one.
 *
 * @param stages List of {@link QueryStage}s.
 * @param config The {@link QueryConfig}. May be null!
 */
public record StagedSimilarityQuery(List<QueryStage> stages, QueryConfig config) {

}
