package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;

/**
 * A {@link TemporalQuery} contains a list of {@link StagedSimilarityQuery}s. Each of them represents a query in a total temporal ordering.
 *
 * @author silvanheller
 * @created 17.02.20
 */
public class TemporalQuery extends Query {

  /**
   * List of {@link StagedSimilarityQuery}s that are part of this {@link TemporalQuery}.
   */
  public final List<StagedSimilarityQuery> queries;

  /**
   * Constructor for the TemporalQuery object.
   *
   * @param queries List of {@link StagedSimilarityQuery}s.
   * @param config  The {@link QueryConfig}. May be null!
   */
  @JsonCreator
  public TemporalQuery(@JsonProperty(value = "queries", required = true) List<StagedSimilarityQuery> queries, @JsonProperty(value = "config", required = false) QueryConfig config) {
    super(config);
    this.queries = queries;
  }

  /**
   * {@inheritDoc}
   */
  public MessageType getMessageType() {
    return MessageType.Q_TEMPORAL;
  }

}
