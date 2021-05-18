package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.config.QueryConfig;

/**
 * A {@link QueryStage} contains a list of {@link QueryTerm}s. This object represents a stage in a {@link StagedSimilarityQuery}.
 */
public class QueryStage {

  /**
   * List of {@link QueryTerm}s that are part of this {@link QueryStage}.
   */
  public final List<QueryTerm> terms;

  /**
   * The {@link QueryConfig} that should be used to configure the query. May be null!
   */
  public final QueryConfig config;

  /**
   * Constructor for the QueryStage object.
   *
   * @param terms  List of {@link QueryTerm}s.
   * @param config The {@link QueryConfig}. May be null!
   */
  @JsonCreator
  public QueryStage(@JsonProperty(value = "terms", required = true) List<QueryTerm> terms, @JsonProperty(value = "config", required = false) QueryConfig config) {
    this.terms = terms;
    this.config = config;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
