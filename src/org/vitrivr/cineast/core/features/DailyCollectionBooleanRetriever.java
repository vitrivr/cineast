package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.retriever.CollectionBooleanRetriever;

import java.util.Collections;

public class DailyCollectionBooleanRetriever extends CollectionBooleanRetriever {
    public DailyCollectionBooleanRetriever() {
        super("features_daily", Collections.singletonList("location"));
    }
}