package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class DescriptionTextSearch extends SolrTextRetriever {

  public static final String DESCRIPTION_TEXT_TABLE_NAME = "features_captioning";

  /**
   * Default constructor for {@link DescriptionTextSearch}
   */
  public DescriptionTextSearch() {
    super(DESCRIPTION_TEXT_TABLE_NAME);
  }
}
