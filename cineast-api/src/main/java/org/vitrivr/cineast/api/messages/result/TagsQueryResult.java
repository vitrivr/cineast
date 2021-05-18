package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.tag.Tag;

/**
 * General-purpose result for any query that expects a list of tags as a result.
 */
public class TagsQueryResult {

  /**
   * The query ID to which this tags query result belongs.
   */
  public final String queryId;

  /**
   * List of tags that represent the result of the tags query.
   */
  public final List<Tag> tags;

  /**
   * Constructor for the TagsQueryResult object.
   *
   * @param queryId String representing the ID of the query to which this part of the result message.
   * @param tags    List of Strings containing the tags that represent the result of the query.
   */
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
