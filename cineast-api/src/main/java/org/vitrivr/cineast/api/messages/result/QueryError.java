package org.vitrivr.cineast.api.messages.result;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query error to communicate an error occurred during querying.
 *
 * @author rgasser
 * @created 03.06.17
 */
public class QueryError implements Message {

  /**
   * ID of the query.
   */
  private final String queryId;

  /**
   * Error message.
   */
  private final String message;

  /**
   * Constructor for the QueryError object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   * @param message String representing the error message.
   */
  public QueryError(String queryId, String message) {
    this.queryId = queryId;
    this.message = message;
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
   * Getter for error message.
   *
   * @return String
   */
  public String getErrorMessage() {
    return this.message;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_ERROR;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
