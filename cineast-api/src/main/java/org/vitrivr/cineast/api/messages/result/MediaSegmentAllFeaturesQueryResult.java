package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;


/**
 * content: Map of all feature categories such as tags, captions, OCR, ASR etc. with their values.
 * used when all features are needed in a single representation
 */
public class MediaSegmentAllFeaturesQueryResult {

  public final String queryId;
  public final Map<String, String[]> featureMap;

  @JsonCreator
  public MediaSegmentAllFeaturesQueryResult(String queryId, Map<String, String[]> featureMap) {
    this.queryId = queryId;
    this.featureMap = featureMap;
  }

}
