package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;

/**
 * A {@link MediaObjectQueryResult} contains a list of {@link MediaObjectDescriptor}s as content of the result message. It is part of a response for queries.
 * <p>
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 * @param content List of {@link MediaObjectDescriptor} of the objects belonging to a query response.
 */
public record MediaObjectQueryResult(String queryId, List<MediaObjectDescriptor> content, MessageType messageType) implements QueryResultMessage<MediaObjectDescriptor> {

  public MediaObjectQueryResult {
    if (messageType != MessageType.QR_OBJECT) {
      throw new IllegalStateException("MessageType was not QR_OBJECT, but " + messageType);
    }
  }

  public MediaObjectQueryResult(String queryId, List<MediaObjectDescriptor> content) {
    this(queryId, content, MessageType.QR_OBJECT);
  }

  @Override
  public Class<MediaObjectDescriptor> contentType() {
    return MediaObjectDescriptor.class;
  }

}
