package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result start to establish the context of the following messages.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 */
public record QueryStart(String queryId, MessageType messageType) implements Message {

  public QueryStart {
    if (messageType != MessageType.QR_START) {
      throw new IllegalStateException("MessageType was not QR_START, but " + messageType);
    }
  }

  public QueryStart(String queryId) {
    this(queryId, MessageType.QR_START);
  }
}
