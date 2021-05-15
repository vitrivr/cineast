package org.vitrivr.cineast.api.messages.result;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class QueryError implements Message {

  /**
   * ID of the query.
   */
  private final String queryId;

  /**
   * Error message.
   */
  private final String message;

  public QueryError(String queryId, String message) {
    this.queryId = queryId;
    this.message = message;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_ERROR;
  }

  public String getQueryId() {
    return queryId;
  }

  public String getErrorMessage() {
    return this.message;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
