package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.vitrivr.cineast.core.data.tag.Tag;

public class DistinctElementsResult {

  public final String queryId;
  public final List<String> distinctElements;

  @JsonCreator
  public DistinctElementsResult(String queryId, List<String> distinctElements) {
    this.queryId = queryId;
    this.distinctElements = distinctElements;
  }
}
