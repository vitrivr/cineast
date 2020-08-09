package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.QueuedTextRetriever;

public class SubtitleFulltextSearch extends QueuedTextRetriever {

  public static final String ASR_TABLE_NAME = "features_asr";

  /**
   * Default constructor for {@link SubtitleFulltextSearch}.
   */
  public SubtitleFulltextSearch() {
    super(ASR_TABLE_NAME);
  }
}
