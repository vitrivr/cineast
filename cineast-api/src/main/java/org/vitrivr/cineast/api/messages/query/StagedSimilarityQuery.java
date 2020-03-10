package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;

/**
 * A {@link StagedSimilarityQuery} contains a list of {@link QueryStage}s. Each of them is used as a filter for the next one.
 */
public class StagedSimilarityQuery {

  public final List<QueryStage> stages;
  public final QueryConfig config;

  public StagedSimilarityQuery(@JsonProperty(value = "stages", required = true) List<QueryStage> stages,
      @JsonProperty(value = "config", required = false) QueryConfig config) {
    this.stages = stages;
    this.config = config;
  }
}
