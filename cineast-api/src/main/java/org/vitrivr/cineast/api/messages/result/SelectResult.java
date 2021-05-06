package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Map;

public class SelectResult {

  public final List<Map<String, String>> columns;

  @JsonCreator
  public SelectResult(List<Map<String, String>> columns) {
    this.columns = columns;
  }

}
