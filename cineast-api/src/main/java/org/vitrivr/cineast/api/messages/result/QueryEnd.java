package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result end to communicate the end of a query result.
 *
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public class QueryEnd implements Message {

  /**
   * The query ID to which this query start message belongs.
   */
  private final String queryId;

  /**
   * Constructor for the QueryEnd object.
   *
   * @param queryId String representing the ID of the query to which this part of the result
   *                message.
   */
  public QueryEnd(String queryId) {
    this.queryId = queryId;
  }

  /**
   * Getter for queryId.
   *
   * @return String
   */
  public String getQueryId() {
    return queryId;
  }

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_END;
  }

  @Override
  public String toString() {
    return "QueryEnd{" +
        "queryId='" + queryId + '\'' +
        '}';
  }
}
