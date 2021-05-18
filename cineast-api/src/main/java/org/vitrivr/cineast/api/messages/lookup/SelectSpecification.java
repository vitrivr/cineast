package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SelectSpecification {

  private String table;
  private List<String> columns;
  private int limit;

  @JsonCreator
  public SelectSpecification(@JsonProperty("table") String table, @JsonProperty("columns") List<String> columns, @JsonProperty("limit") int limit) {
    this.table = table;
    this.columns = columns;
    this.limit = limit;
  }


  public int getLimit() {
    return limit;
  }

  public List<String> getColumns() {
    return columns;
  }

  public String getTable() {
    return table;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
