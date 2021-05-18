package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A {@link FeaturesTextCategoryQueryResult} contains a list of Strings as content of the result message. All features for a given category and element (i.e. segment, object). Only used for String features (e.g. OCR, ASR) queries.
 */
public class FeaturesTextCategoryQueryResult {

  /**
   * The query ID to which this features text category query result belongs.
   */
  public final String queryId;

  /**
   * List of features that belong to a given category and element.
   */
  public final List<String> featureValues;

  /**
   * The category for which the feature values were requested.
   */
  public final String category;

  /**
   * The element for which the feature values were requested. can refer to anything with an ID (i.e. segment, object).
   */
  public final String elementID;

  /**
   * Constructor for the FeaturesTextCategoryQueryResult object.
   *
   * @param queryId       String representing the ID of the query to which this part of the result message.
   * @param featureValues List of Strings containing the feature values for the given element and category.
   * @param category      Category for which the feature values were requested.
   * @param elementID     Element for which the feature values were requested.
   */
  @JsonCreator
  public FeaturesTextCategoryQueryResult(String queryId, List<String> featureValues, String category, String elementID) {
    this.queryId = queryId;
    this.featureValues = featureValues;
    this.category = category;
    this.elementID = elementID;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
