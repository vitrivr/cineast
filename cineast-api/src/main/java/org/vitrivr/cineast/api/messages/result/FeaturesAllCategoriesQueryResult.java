package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;


/**
 * content: Map of all feature categories such as tags, captions, OCR, ASR etc. with their values. Used when all features are needed in a single representation
 */
public class FeaturesAllCategoriesQueryResult {

  public final String queryId;
  /**
   * <category, all features for this category and the given id>
   */
  public final Map<String, Object[]> featureMap;

  /**
   * can refer to anything with an ID (i.e. segment, object)
   */
  public final String elementID;

  @JsonCreator
  public FeaturesAllCategoriesQueryResult(String queryId, Map<String, Object[]> featureMap, String elementID) {
    this.queryId = queryId;
    this.featureMap = featureMap;
    this.elementID = elementID;
  }

}
