package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.retriever.CollectionBooleanRetriever;

import java.util.Arrays;

public class WSDMTICollectionBooleanRetriever extends CollectionBooleanRetriever {
    public WSDMTICollectionBooleanRetriever() {
        super("features_wsdmtiannotations", Arrays.asList("annotation","object"));
    }
}