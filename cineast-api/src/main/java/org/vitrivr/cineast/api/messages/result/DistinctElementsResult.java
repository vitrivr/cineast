package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DistinctElementsResult {

  public final String queryId;
  public final List<String> distinctElements;

  @JsonCreator
  public DistinctElementsResult(String queryId, List<String> distinctElements) {
    this.queryId = queryId;
    this.distinctElements = distinctElements;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
