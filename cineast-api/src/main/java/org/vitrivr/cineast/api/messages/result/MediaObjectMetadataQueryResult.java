package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

/**
 * A {@link MediaSegmentMetadataQueryResult} contains a list of {@link MediaObjectMetadataDescriptor}s as content of the result message. It is part of a response for queries.
 *
 * @author rgasser
 * @created 14.02.17
 */
public class MediaObjectMetadataQueryResult extends AbstractQueryResultMessage<MediaObjectMetadataDescriptor> {

  /**
   * Constructor for the MediaObjectMetadataQueryResult object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   * @param content List of {@link MediaObjectMetadataDescriptor} of the metadata descriptors of objects belonging to to a query response.
   */
  @JsonCreator
  public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
    super(queryId, MediaObjectMetadataDescriptor.class, content);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_METADATA_O;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
