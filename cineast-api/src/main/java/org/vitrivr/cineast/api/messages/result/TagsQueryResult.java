package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.core.data.tag.Tag;

import java.util.List;

/**
 * Object that contains the top frequent tags and their count in a result set
 */

public class TagsQueryResult {
  
  public final String queryId;
  public final List<Tag> tags;
  
  @JsonCreator
  public TagsQueryResult(String queryId, List<Tag> tags) {
    this.queryId = queryId;
    this.tags = tags;
  }
}
