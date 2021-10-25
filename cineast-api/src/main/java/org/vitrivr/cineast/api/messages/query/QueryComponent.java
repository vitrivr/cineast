package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;

/**
 * The wording is suboptimal. A {@link QueryComponent} has only one containerID, but multiple {@link AbstractQueryTermContainer}s are created out of it.
 * <p>
 * These all have the {@link QueryComponent#containerId} of their parent.
 *
 * @deprecated use {@link TemporalQuery} instead, which uses {@link StagedSimilarityQuery} which contains {@link QueryStage} which in turn contains {@link QueryTerm}
 */
@Deprecated
public class QueryComponent {

  /**
   * List of {@link QueryTerm}s in this {@link QueryComponent}.
   */
  private final List<QueryTerm> terms;

  /**
   * The client-generated uuid for this container for reference purposes
   */
  public final int containerId;

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Constructor for QueryComponent.
   */
  @JsonCreator
  public QueryComponent(@JsonProperty("terms") List<QueryTerm> terms, @JsonProperty("containerId") int containerId) {
    this.terms = terms;
    this.containerId = containerId;
  }

  public List<QueryTerm> getTerms() {
    return this.terms;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
