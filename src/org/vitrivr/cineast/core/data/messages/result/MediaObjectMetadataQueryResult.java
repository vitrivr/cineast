package org.vitrivr.cineast.core.data.messages.result;

import static org.vitrivr.cineast.core.data.messages.interfaces.MessageType.QR_METADATA_O;

import java.util.List;

import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MediaObjectMetadataQueryResult extends AbstractQueryResultMessage<MediaObjectMetadataDescriptor> {

    @JsonCreator
    public MediaObjectMetadataQueryResult(String queryId, List<MediaObjectMetadataDescriptor> content) {
        super(queryId, content);
    }


    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return QR_METADATA_O;
    }
}
