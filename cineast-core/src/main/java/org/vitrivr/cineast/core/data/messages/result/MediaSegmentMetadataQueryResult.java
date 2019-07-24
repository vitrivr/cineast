package org.vitrivr.cineast.core.data.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.List;

public class MediaSegmentMetadataQueryResult extends AbstractQueryResultMessage<MediaSegmentMetadataDescriptor> {

    @JsonCreator
    public MediaSegmentMetadataQueryResult(String queryId, List<MediaSegmentMetadataDescriptor> content) {
        super(queryId, content);
    }


    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_METADATA_S;
    }
}
