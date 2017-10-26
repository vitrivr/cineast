package org.vitrivr.cineast.core.data.messages.result;

import java.util.List;

import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class SegmentQueryResult extends AbstractQueryResultMessage<SegmentDescriptor> {
    /**
     *
     * @param content
     */
    @JsonCreator
    public SegmentQueryResult(String queryId, List<SegmentDescriptor> content) {
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
