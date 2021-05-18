package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * General-purpose result for any query that expects a list of tag IDs for an element ID as a result.
 */
public class TagIDsForElementQueryResult {

  /**
   * The query ID to which this tags query result belongs.
   */
  public final String queryId;

  /**
   * List of tag IDs that represent the result of the tags query.
   */
  public final List<String> tagIDs;

  /**
   * The element ID to which the tag IDs have been retrieved.
   */
  public final String elementID;

  /**
   * Constructor for the TagIDsForElementQueryResult object.
   *
   * @param queryId   String representing the ID of the query to which this part of the result message.
   * @param tags      List of Strings containing the tag IDs that belong to the element ID and represent the result of the query.
   * @param elementID String representing the element ID of the element of which the tag IDs were looked up for.
   */
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
