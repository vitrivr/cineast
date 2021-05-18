package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;

/**
 * A {@link MediaSegmentQueryResult} contains a list of {@link MediaSegmentDescriptor}s as content of the result message. It is part of a response for queries.
 */
public class MediaSegmentQueryResult extends AbstractQueryResultMessage<MediaSegmentDescriptor> {

  /**
   * Constructor for the SimilarityQueryResult object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   * @param content List of {@link MediaSegmentDescriptor} of the segments belonging to to a query response.
   */
  @JsonCreator
  public MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content) {
    super(queryId, MediaSegmentDescriptor.class, content);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_SEGMENT;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
