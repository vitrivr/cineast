package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message from the requester specifying from which table and which column information should be fetched.
 *
 * @author silvanheller
 * @created 22.07.20
 */
public class ColumnSpecification implements Message {

  /**
   * The requested table.
   */
  private final String table;

  /**
   * The requested column.
   */
  private final String column;

  /**
   * Constructor for the ColumnSpecification object.
   *
   * @param column requested column.
   * @param table  requested table.
   */
  @JsonCreator
  public ColumnSpecification(@JsonProperty("column") String column, @JsonProperty("table") String table) {
    this.column = column;
    this.table = table;
  }

  public String getTable() {
    return table;
  }

  public String getColumn() {
    return column;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
