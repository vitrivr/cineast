package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import java.util.List;

public class MediaObjectMetadataQueryResult extends AbstractQueryResultMessage<MediaObjectMetadataDescriptor> {

  @JsonCreator
  public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
    super(queryId, MediaObjectMetadataDescriptor.class, content);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_METADATA_O;
  }
}
