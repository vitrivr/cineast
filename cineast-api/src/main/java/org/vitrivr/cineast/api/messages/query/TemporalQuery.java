package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.Query;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * This object represents a temporal-query message of temporal query version 2, i.e. a request for a temporally staged similarity-search.
 */
public record TemporalQuery(@JsonProperty(required = true) List<StagedSimilarityQuery> queries, List<MetadataAccessSpecification> metadataAccessSpec, TemporalQueryConfig config, @JsonProperty(required = true) MessageType messageType) implements Query {

  public TemporalQuery {
    if (messageType != MessageType.Q_TEMPORAL) {
      throw new IllegalStateException("MessageType was not Q_TEMPORAL, but " + messageType);
    }
  }

  /**
   * Convenience getter for timeDistances.
   *
   * @return List<Float>
   */
  @JsonIgnore
  public List<Float> getTimeDistances() {
    return config().timeDistances;
  }

  /**
   * Convenience getter for maxLength.
   *
   * @return Float
   */
  @JsonIgnore
  public Float getMaxLength() {
    return config().maxLength;
  }

}
