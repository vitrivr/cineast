package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * @author rgasser
 * @version 1.0
 * @created 03.06.17
 */
public class QueryError implements Message {
    /** ID of the query. */
    private final String queryId;

    /** Error message. */
    private final String message;

    /**
     * Default constructor for QueryError object.
     *
     * @param queryId ID of the query.
     * @param message Error message.
     */
    public QueryError(String queryId, String message) {
        this.queryId = queryId;
        this.message = message;
    }

    /**
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_ERROR;
    }

    /**
     *
     * @return
     */
    public String getQueryId() {
        return queryId;
    }
    
    public String getErrorMessage(){
      return this.message;
    }
}
