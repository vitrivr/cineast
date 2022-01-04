package org.vitrivr.cineast.core.features;

import java.util.Collections;
import org.vitrivr.cineast.core.features.retriever.CollectionBooleanRetriever;

public class DailyCollectionBooleanRetriever extends CollectionBooleanRetriever {

  public DailyCollectionBooleanRetriever() {
    super("features_daily", Collections.singletonList("location"));
  }
}