package org.vitrivr.cineast.core.data.messages.result;

import java.util.List;

import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class MediaSegmentQueryResult extends AbstractQueryResultMessage<MediaSegmentDescriptor> {
    /**
     *
     * @param content
     */
    @JsonCreator
    public MediaSegmentQueryResult(String queryId, List<MediaSegmentDescriptor> content) {
        super(queryId, content);
    }

    /**
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SEGMENT;
    }
}
