package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * A {@link MediaSegmentMetadataQueryResult} contains a list of {@link MediaObjectMetadataDescriptor}s as content of the result message. It is part of a response for queries.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 * @param content List of {@link MediaObjectMetadataDescriptor} of the metadata descriptors of objects belonging to a query response.
 */
public record MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content, MessageType messageType) implements QueryResultMessage<MediaObjectMetadataDescriptor> {

  public MediaObjectMetadataQueryResult {
    if (messageType != MessageType.QR_METADATA_O) {
      throw new IllegalStateException("MessageType was not QR_METADATA_O, but " + messageType);
    }
  }

  public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
    this(queryId, content, MessageType.QR_METADATA_O);
  }

  @Override
  public Class<MediaObjectMetadataDescriptor> contentType() {
    return MediaObjectMetadataDescriptor.class;
  }
}
