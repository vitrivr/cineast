package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Contains a list of all features and the IDs of the objects they belong to for a given table/entity name.
 */
public class FeaturesByEntityQueryResult {

  /**
   * The query ID to which this result belongs to.
   */
  public final String queryId;

  /**
   * Feature list containing IDs/feature array for every object ID for every feature in the table/entity.
   */
  public final ArrayList<HashMap<String, Object>> featureMap;

  /**
   * The category for which the features of all objects were requested.
   */
  public final String entityName;

  /**
   * Constructor for the FeaturesTextCategoryQueryResult object.
   *
   * @param queryId    Query ID as a string to which this result belongs to.
   * @param featureMap Map containing a list of IDs/feature array for every object ID for every feature in the category.
   * @param entityName Entity name for which the features of all objects were requested.
   */
  @JsonCreator
  public FeaturesByEntityQueryResult(String queryId, ArrayList<HashMap<String, Object>> featureMap, String entityName) {
    this.queryId = queryId;
    this.featureMap = featureMap;
    this.entityName = entityName;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

}
