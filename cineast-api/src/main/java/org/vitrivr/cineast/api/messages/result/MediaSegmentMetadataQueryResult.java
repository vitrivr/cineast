package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;

/**
 * A {@link MediaSegmentMetadataQueryResult} contains a list of {@link MediaSegmentMetadataDescriptor}s as content of the result message. It is part of a response for queries.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 * @param content List of {@link MediaSegmentMetadataDescriptor} of the metadata descriptors of segments belonging to a query response.
 */
public record MediaSegmentMetadataQueryResult(String queryId, List<MediaSegmentMetadataDescriptor> content, MessageType messageType) implements QueryResultMessage<MediaSegmentMetadataDescriptor> {

  public MediaSegmentMetadataQueryResult {
    if (messageType != MessageType.QR_METADATA_S) {
      throw new IllegalStateException("MessageType was not QR_METADATA_S, but " + messageType);
    }
  }

  public MediaSegmentMetadataQueryResult(String queryId, List<MediaSegmentMetadataDescriptor> content) {
    this(queryId, content, MessageType.QR_METADATA_S);
  }

  @Override
  public Class<MediaSegmentMetadataDescriptor> contentType() {
    return MediaSegmentMetadataDescriptor.class;
  }

}
