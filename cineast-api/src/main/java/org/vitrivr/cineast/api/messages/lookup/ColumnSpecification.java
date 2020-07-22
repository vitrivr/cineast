package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class ColumnSpecification implements Message {

  private String table;
  private String column;

  public String getTable() {
    return table;
  }

  public String getColumn() {
    return column;
  }

  @JsonCreator
  public ColumnSpecification(@JsonProperty("column") String column, @JsonProperty("table") String table) {
    this.column = column;
    this.table = table;
  }

  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
