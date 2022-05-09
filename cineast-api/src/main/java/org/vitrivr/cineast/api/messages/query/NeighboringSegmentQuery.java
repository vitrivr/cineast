package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.Query;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * A {@link NeighboringSegmentQuery} represents a query for neighbors of a given segment ID.
 *
 * @param segmentId          ID of the {@link MediaSegmentDescriptor} for which neighbors should be retrieved.
 * @param count              Number of neighbors that should be retrieved.
 * @param metadataAccessSpec Which metadata should be fetched
 * @param config             The query configuration
 */
public record NeighboringSegmentQuery(@JsonProperty(required = true) String segmentId, @JsonProperty(required = true) Integer count, List<MetadataAccessSpecification> metadataAccessSpec, QueryConfig config, @JsonProperty(required = true) MessageType messageType) implements Query {

  public NeighboringSegmentQuery {
    if (messageType != MessageType.Q_NESEG) {
      throw new IllegalStateException("MessageType was not Q_NESEG, but " + messageType);
    }
  }
}
