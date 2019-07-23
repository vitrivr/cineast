package org.vitrivr.cineast.core.features;

import java.util.Arrays;

public class DailyRangeBooleanRetriever extends RangeBooleanRetriever {
    public DailyRangeBooleanRetriever() {
        super("features_daily", Arrays.asList("heart_rate","skim_temp","steps","calories","gsr"));
    }
}
