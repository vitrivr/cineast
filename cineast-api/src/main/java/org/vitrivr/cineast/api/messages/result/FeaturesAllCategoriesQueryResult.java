package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A {@link FeaturesAllCategoriesQueryResult} contains a list of Strings as content of the result message. Map of all feature categories such as tags, captions, OCR, ASR etc. with their values. Used when all features are needed in a single representation.
 *
 * @author silvanheller
 * @created 04.12.20
 */
public class FeaturesAllCategoriesQueryResult {

  /**
   * The query ID to which this features all categories query result belongs.
   */
  public final String queryId;

  /**
   * <category, all features for this category and the given id>
   */
  public final Map<String, Object[]> featureMap;

  /**
   * can refer to anything with an ID (i.e. segment, object)
   */
  public final String elementID;

  /**
   * Constructor for the FeaturesTextCategoryQueryResult object.
   *
   * @param queryId    String representing the ID of the query to which this part of the result
   *                   message.
   * @param featureMap Map that maps strings to all features for this category for a given element
   *                   ID.
   * @param elementID  Element for which the feature values were requested.
   */
  @JsonCreator
  public FeaturesAllCategoriesQueryResult(String queryId, Map<String, Object[]> featureMap, String elementID) {
    this.queryId = queryId;
    this.featureMap = featureMap;
    this.elementID = elementID;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
