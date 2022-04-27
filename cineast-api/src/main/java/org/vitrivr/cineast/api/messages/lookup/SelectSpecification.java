package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SelectSpecification(String table, List<String> columns, int limit) {

  public List<String> getColumns() {
    return columns;
  }

  public String getTable() {
    return table;
  }

}
