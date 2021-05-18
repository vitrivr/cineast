package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

/**
 * A {@link SimilarityQuery} contains a list of {@link QueryComponent}s. This object represents a similarity-query message, i.e. a request for a similarity-search.
 */
public class SimilarityQuery extends Query {

  /**
   * List of {@link QueryComponent}s that are part of this {@link SimilarityQuery}.
   */
  private final List<QueryComponent> components;

  /**
   * Constructor for the SimilarityQuery object.
   *
   * @param components List of {@link QueryComponent}s.
   * @param config     The {@link ReadableQueryConfig}. May be null!
   */
  @JsonCreator
  public SimilarityQuery(@JsonProperty(value = "containers", required = true) List<QueryComponent> components,
      @JsonProperty(value = "config", required = false) QueryConfig config) {
    super(config);
    this.components = components;
  }

  /**
   * Getter for containers.
   */
  public List<QueryComponent> getComponents() {
    return this.components;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.Q_SIM;
  }
}
