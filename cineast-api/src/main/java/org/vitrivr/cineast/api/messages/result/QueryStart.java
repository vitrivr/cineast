package org.vitrivr.cineast.api.messages.result;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result start to establish the context of the following messages.
 */
public class QueryStart implements Message {

  /**
   * Unique ID of the QueryStart message. This ID establishes a context which is important for all further communication.
   */
  private final String queryId;

  /**
   * Constructor for the QueryStart object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   */
  public QueryStart(String queryId) {
    this.queryId = queryId;
  }

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
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
