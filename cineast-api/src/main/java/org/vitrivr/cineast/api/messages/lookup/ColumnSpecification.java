package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message from the requester to transfer table and column information of a request.
 *
 * @author silvanheller
 * @created 22.07.20
 */
public class ColumnSpecification implements Message {

  /**
   * The requested table.
   */
  private String table;

  /**
   * The requested column.
   */
  private String column;

  /**
   * Constructor for the ColumnSpecification object.
   *
   * @param column requested column.
   * @param table  requested table.
   */
  @JsonCreator
  public ColumnSpecification(@JsonProperty("column") String column,
      @JsonProperty("table") String table) {
    this.column = column;
    this.table = table;
  }

  /**
   * Getter for table.
   *
   * @return {@link Credentials}
   */
  public String getTable() {
    return table;
  }

  /**
   * Getter for column.
   *
   * @return {@link Credentials}
   */
  public String getColumn() {
    return column;
  }

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
