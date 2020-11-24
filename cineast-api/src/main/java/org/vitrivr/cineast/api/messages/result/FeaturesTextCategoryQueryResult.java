package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

/**
 * All features for a given category and element (i.e. segment, object). Only used for String features (e.g. OCR, ASR)
 */
public class FeaturesTextCategoryQueryResult {

  public final String queryId;
  public final List<String> featureValues;
  public final String category;
  public final String elementID;

  @JsonCreator
  public FeaturesTextCategoryQueryResult(String queryId, List<String> featureValues, String category, String elementID) {
    this.queryId = queryId;
    this.featureValues = featureValues;
    this.category = category;
    this.elementID = elementID;
  }

}
