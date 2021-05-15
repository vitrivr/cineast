package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.tag.Tag;

/**
 * General-purpose result for any query that expects a list of tags as a result
 */
public class TagsQueryResult {

  public final String queryId;
  public final List<Tag> tags;

  @JsonCreator
  public TagsQueryResult(String queryId, List<Tag> tags) {
    this.queryId = queryId;
    this.tags = tags;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
