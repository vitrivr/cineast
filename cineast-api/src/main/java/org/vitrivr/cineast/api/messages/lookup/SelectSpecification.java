package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;

public class SelectSpecification extends AbstractMessage {

  private final String table;
  private final List<String> columns;
  private final int limit;

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

}
