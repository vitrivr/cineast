package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;

public class MediaSegmentMetadataQueryResult extends AbstractQueryResultMessage<MediaSegmentMetadataDescriptor> {

    @JsonCreator
    public MediaSegmentMetadataQueryResult(String queryId, List<MediaSegmentMetadataDescriptor> content) {
        super(queryId, MediaSegmentMetadataDescriptor.class, content);
    }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_METADATA_S;
  }

}
