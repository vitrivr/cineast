package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message from the requester specifying from which table and which column information should be fetched.
 */
public class ColumnSpecification extends AbstractMessage {

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
}
