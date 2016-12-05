package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class DescriptionTextSearch extends SolrTextRetriever {

  @Override
  protected String getEntityName() {
    return "features_description";
  }

  @Override
  protected float getMaxDist() {
    return 10;
  }

}
