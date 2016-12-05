package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class SubtitleFulltextSearch extends SolrTextRetriever {

  @Override
  protected String getEntityName() {
    return "features_asr";
  }

  @Override
  protected float getMaxDist() {
    return 10f;
  }

}
