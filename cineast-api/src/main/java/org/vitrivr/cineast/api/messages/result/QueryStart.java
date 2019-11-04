package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class QueryStart implements Message {
    /**
     * Unique ID of the QueryStart message. This ID establishes a context which is importent
     * for all further communication.
     */
    private final String queryId;

    /**
     * Default constructor; generates the QueryId as random UUID.
     */
    public QueryStart(String queryId) {
        this.queryId = queryId;
    }

    /**
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_START;
    }

    /**
     *
     * @return
     */
    public String getQueryId() {
        return queryId;
    }
}
