package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

/**
 * content: List of all entries for a specific metadata category such as tags, captions, OCR or ASR.
 * used to retrieve entries of a single metadata category
 */
public class MediaSegmentFeatureQueryResult {

  public final String queryId;
  public final List<String> content;

  @JsonCreator
  public MediaSegmentFeatureQueryResult(String queryId, List<String> content) {
    this.queryId = queryId;
    this.content = content;
  }

}
