package org.vitrivr.cineast.api.messages.result;

import java.util.List;

import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MediaObjectMetadataQueryResult extends AbstractQueryResultMessage<MediaObjectMetadataDescriptor> {

    @JsonCreator
    public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
        super(queryId, MediaObjectMetadataDescriptor.class, content);
    }


    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_METADATA_O;
    }
}
