package org.vitrivr.cineast.api.messages.result;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message for a query result end to communicate the end of a query result.
 */
public class QueryEnd extends AbstractMessage {

  /**
   * The query ID to which this query end message belongs.
   */
  private final String queryId;

  /**
   * Constructor for the QueryEnd object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   */
  public QueryEnd(String queryId) {
    this.queryId = queryId;
  }

  public String getQueryId() {
    return queryId;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_END;
  }

}
