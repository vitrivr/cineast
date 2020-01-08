package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

public class MediaObjectMetadataQueryResult extends AbstractQueryResultMessage<MediaObjectMetadataDescriptor> {

  @JsonCreator
  public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
    super(queryId, content);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_METADATA_O;
  }
}
