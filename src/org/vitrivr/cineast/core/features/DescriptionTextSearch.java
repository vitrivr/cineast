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

  @Override
  public void processSegment(SegmentContainer shot) {
    /* TODO: Not implemented because densecap extraction is not integrated into pipeline yet. */
  }

  @Override
  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    return sc.getText().split(" ");
  }
}
