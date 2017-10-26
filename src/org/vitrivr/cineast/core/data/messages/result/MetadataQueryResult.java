package org.vitrivr.cineast.core.data.messages.result;

import static org.vitrivr.cineast.core.data.messages.interfaces.MessageType.QR_METADATA;

import java.util.List;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MetadataQueryResult extends AbstractQueryResultMessage<MultimediaMetadataDescriptor> {

    @JsonCreator
    public MetadataQueryResult(String queryId, List<MultimediaMetadataDescriptor> content) {
        super(queryId, content);
    }


    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return QR_METADATA;
    }
}
