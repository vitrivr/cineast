package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

/**
 * featureValues: List of all entries for a specific feature category such as tags, captions, OCR or ASR.
 *
 */
public class MediaSegmentFeatureQueryResult {

  public final String queryId;
  public final List<String> featureValues;

  @JsonCreator
  public MediaSegmentFeatureQueryResult(String queryId, List<String> featureValues) {
    this.queryId = queryId;
    this.featureValues = featureValues;
  }

}
