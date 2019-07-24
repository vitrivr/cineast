package org.vitrivr.cineast.core.data.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class MediaObjectQueryResult extends AbstractQueryResultMessage<MediaObjectDescriptor> {

    /**
     *
     * @param queryId
     * @param content
     */
    @JsonCreator
    public MediaObjectQueryResult(String queryId, List<MediaObjectDescriptor> content) {
        super(queryId, content);
    }

    /**
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_OBJECT;
    }
}
