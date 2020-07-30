package org.vitrivr.cineast.core.features.multi;

import java.util.Iterator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

public abstract class MultiFeature<T extends Extractor & Retriever> implements MultiExtractor, MultiRetriever {
  public abstract Iterator<T> getSubOperators();

  @Override
  public void finish() {
    MultiExtractor.super.finish();
  }
}
