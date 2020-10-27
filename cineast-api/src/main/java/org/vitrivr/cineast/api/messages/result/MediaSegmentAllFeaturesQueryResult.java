package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;


/**
 * content: List of all metadata categories such as tags, captions, OCR, ASR etc.
 * used when all metadata is needed in a single representation
 */
public class MediaSegmentAllFeaturesQueryResult {

  public final String queryId;
  public final List<String[]> content;

  @JsonCreator
  public MediaSegmentAllFeaturesQueryResult(String queryId, List<String[]> content) {
    this.queryId = queryId;
    this.content = content;
  }

}
