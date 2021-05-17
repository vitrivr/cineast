package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A {@link DistinctElementsResult} contains a list of distinct element IDs.
 *
 * @author silvanheller
 * @created 22.07.20
 */
public class DistinctElementsResult {

  /**
   * The query ID to which this distinct elements result belongs.
   */
  public final String queryId;

  /**
   * List of distinct elements to be retrieved by a find distinct column request.
   */
  public final List<String> distinctElements;

  /**
   * Constructor for the FeaturesTextCategoryQueryResult object.
   *
   * @param queryId          String representing the ID of the query to which this part of the
   *                         result message.
   * @param distinctElements List of Strings containing distinct elements.
   */
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
