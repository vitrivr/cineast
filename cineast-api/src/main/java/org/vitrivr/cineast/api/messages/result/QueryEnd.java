package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result end to communicate the end of a query result.
 *
 * @param queryId String representing the ID of the query to which this part of the result message.
 */
public record QueryEnd(String queryId, MessageType messageType) implements Message {

  public QueryEnd(String queryId) {
    this(queryId, MessageType.QR_END);
  }

}
