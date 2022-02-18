package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

/**
 * A {@link SimilarityQuery} contains a list of {@link QueryTerm}s. This object represents a similarity-query message, i.e. a request for a similarity-search.
 */
public class SimilarityQuery extends Query {

  /**
   * List of {@link QueryTerm}s that are part of this {@link SimilarityQuery}.
   */
  private final List<QueryTerm> terms;

  /**
   * Constructor for the SimilarityQuery object.
   *
   * @param terms List of {@link QueryTerm}s.
   * @param config     The {@link ReadableQueryConfig}. May be null!
   */
  @JsonCreator
  public SimilarityQuery(@JsonProperty(value = "terms", required = true) List<QueryTerm> terms, @JsonProperty(value = "config") QueryConfig config) {
    super(config);
    this.terms = terms;
  }

  /**
   * Getter for containers.
   */
  public List<QueryTerm> getTerms() {
    return this.terms;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.Q_SIM;
  }
}
