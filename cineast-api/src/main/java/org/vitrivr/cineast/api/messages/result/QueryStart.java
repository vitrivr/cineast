package org.vitrivr.cineast.api.messages.result;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class QueryStart implements Message {

  /**
   * Unique ID of the QueryStart message. This ID establishes a context which is important for all further communication.
   */
  private final String queryId;

  /**
   * Default constructor; generates the QueryId as random UUID.
   */
  public QueryStart(String queryId) {
    this.queryId = queryId;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_START;
  }

  public String getQueryId() {
    return queryId;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
