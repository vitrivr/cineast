package org.vitrivr.cineast.core.features;

import java.util.Arrays;
import org.vitrivr.cineast.core.features.retriever.CollectionBooleanRetriever;

public class WSDMTICollectionBooleanRetriever extends CollectionBooleanRetriever {

  public WSDMTICollectionBooleanRetriever() {
    super("features_wsdmtiannotations", Arrays.asList("annotation", "object"));
  }
}