package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * A {@link MediaSegmentQueryResult} contains a list of {@link MediaSegmentDescriptor}s as content of the result message. It is part of a response for queries.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 * @param content List of {@link MediaSegmentDescriptor} of the segments belonging to a query response.
 */
public record MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content, MessageType messageType) implements QueryResultMessage<MediaSegmentDescriptor> {

  public MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content) {
    this(queryId, content, MessageType.QR_SEGMENT);
  }

  @Override
  public Class<MediaSegmentDescriptor> contentType() {
    return MediaSegmentDescriptor.class;
  }

}
