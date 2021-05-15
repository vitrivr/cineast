package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TagIDsForElementQueryResult {

  public final String queryId;

  public final List<String> tagIDs;

  public final String elementID;

  public TagIDsForElementQueryResult(String queryId, List<String> tags, String elementID) {
    this.queryId = queryId;
    this.tagIDs = tags;
    this.elementID = elementID;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
