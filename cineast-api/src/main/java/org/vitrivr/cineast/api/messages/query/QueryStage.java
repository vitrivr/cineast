package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A {@link QueryStage} contains a list of {@link QueryTerm}s. This object represents a stage in a {@link StagedSimilarityQuery}.
 */
public class QueryStage {

  /**
   * List of {@link QueryTerm}s that are part of this {@link QueryStage}.
   */
  public final List<QueryTerm> terms;

  /**
   * Constructor for the QueryStage object.
   *
   * @param terms  List of {@link QueryTerm}s.
   */
  @JsonCreator
  public QueryStage(@JsonProperty(value = "terms", required = true) List<QueryTerm> terms) {
    this.terms = terms;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
