package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class OCRSearch extends SolrTextRetriever {

  @Override
  protected String getEntityName() {
    return "features_ocr";
  }

  @Override
  protected float getMaxDist() {
    return 10f;
  }

}
