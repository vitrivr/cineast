package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;

/**
 * A {@link MediaSegmentMetadataQueryResult} contains a list of {@link MediaSegmentMetadataDescriptor}s as content of the result message. It is part of a response for queries.
 */
public class MediaSegmentMetadataQueryResult extends AbstractQueryResultMessage<MediaSegmentMetadataDescriptor> {

  /**
   * Constructor for the MediaSegmentMetadataQueryResult object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   * @param content List of {@link MediaSegmentMetadataDescriptor} of the metadata descriptors of segments belonging to to a query response.
   */
  @JsonCreator
  public MediaSegmentMetadataQueryResult(String queryId, List<MediaSegmentMetadataDescriptor> content) {
    super(queryId, MediaSegmentMetadataDescriptor.class, content);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_METADATA_S;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
