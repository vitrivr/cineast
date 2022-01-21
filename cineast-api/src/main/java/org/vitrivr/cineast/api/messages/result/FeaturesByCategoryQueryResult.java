package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Contains lists of all features and the IDs of the objects they belong to, mapped by table/entity name.
 */
public class FeaturesByCategoryQueryResult {

  /**
   * The query ID to which this result belongs to.
   */
  public final String queryId;

  /**
   * Feature map containing a list of IDs/feature array for every object ID for every feature in the category.
   */
  public final Map<String, ArrayList<HashMap<String, Object>>> featureMap;

  /**
   * The category for which the features of all objects were requested.
   */
  public final String category;

  /**
   * Constructor for the FeaturesTextCategoryQueryResult object.
   *
   * @param queryId    Query ID as a string to which this result belongs to.
   * @param featureMap Map containing a list of IDs/feature array for every object ID for every feature in the category.
   * @param category   Category for which the features of all objects were requested.
   */
  @JsonCreator
  public FeaturesByCategoryQueryResult(String queryId, Map<String, ArrayList<HashMap<String, Object>>> featureMap, String category) {
    this.queryId = queryId;
    this.featureMap = featureMap;
    this.category = category;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

}
