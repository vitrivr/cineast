package org.vitrivr.cineast.core.data.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.List;

import static org.vitrivr.cineast.core.data.messages.interfaces.MessageType.QR_METADATA;

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
    public MessageType getMessagetype() {
        return QR_METADATA;
    }
}
