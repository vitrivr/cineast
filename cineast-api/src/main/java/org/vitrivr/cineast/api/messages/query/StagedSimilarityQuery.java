package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.config.QueryConfig;

/**
 * A {@link StagedSimilarityQuery} contains a list of {@link QueryStage}s. Each of them is used as a filter for the next one.
 */
public class StagedSimilarityQuery {

  /**
   * List of {@link QueryStage}s that are part of this {@link StagedSimilarityQuery}.
   */
  private final List<QueryStage> stages;

  /**
   * The {@link QueryConfig} that should be used to configure the query. May be null!
   */
  private final QueryConfig config;

  /**
   * Constructor for the StagedSimilarityQuery object.
   *
   * @param stages List of {@link QueryStage}s.
   * @param config The {@link QueryConfig}. May be null!
   */
  public StagedSimilarityQuery(@JsonProperty(value = "stages", required = true) List<QueryStage> stages,
      @JsonProperty(value = "config", required = false) QueryConfig config) {
    this.stages = stages;
    this.config = config;
  }

  public List<QueryStage> getStages() {
    return stages;
  }

  public QueryConfig getConfig() {
    return config;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
