package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.util.audio.HPCP;


public class AverageHPCP30F36B extends AverageHPCP {
    private final static float MIN_FREQUENCY = 50.0f;

    private final static float MAX_FREQUENCY = 5000.0f;

    /**
     * Default constructor.
     */
    public AverageHPCP30F36B() {
        super("features_avghpcp30f36b", MIN_FREQUENCY, MAX_FREQUENCY, HPCP.Resolution.THIRDSEMITON, 30);
    }
}
