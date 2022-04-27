package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query error to communicate an error occurred during querying.
 *
 * @param message Error message
 * @param queryId String representing the ID of the query to which this part of the result message.
 */
public record QueryError(String queryId, String message, MessageType messageType) implements Message {

  public QueryError(String queryId, String message) {
    this(queryId, message, MessageType.QR_ERROR);
  }

}
