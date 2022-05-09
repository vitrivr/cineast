package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.Query;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * A {@link SegmentQuery} represents a segment-query message, i.e. a lookup for a segment ID specified.
 */
public record SegmentQuery(@JsonProperty(required = true) String segmentId, @JsonProperty(required = true) List<MetadataAccessSpecification> metadataAccessSpec, QueryConfig config, @JsonProperty(required = true) MessageType messageType) implements Query {

  public SegmentQuery {
    if (messageType != MessageType.Q_SEG) {
      throw new IllegalStateException("MessageType was not Q_SEG, but " + messageType);
    }
  }

}
