package org.vitrivr.cineast.api.messages.result;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result start to establish the context of the following messages.
 *
 * @author rgasser
 * @created 22.01.17
 */
public class QueryStart implements Message {

  /**
   * The query ID to which this query start message belongs. Unique ID of the QueryStart message.
   * This ID establishes a context which is important for all further communication.
   */
  private final String queryId;

  /**
   * Constructor for the QueryStart object.
   *
   * @param queryId String representing the ID of the query to which this part of the result
   *                message.
   */
  public QueryStart(String queryId) {
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
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_START;
  }

  @Override
  public String toString() {
    return "QueryStart{" +
        "queryId='" + queryId + '\'' +
        '}';
  }
}
