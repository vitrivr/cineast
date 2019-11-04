package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.util.audio.HPCP;

/**
 * @author rgasser
 * @version 1.0
 * @created 25.04.17
 */
public class AverageHPCP20F36B extends AverageHPCP {
    private final static float MIN_FREQUENCY = 50.0f;

    private final static float MAX_FREQUENCY = 5000.0f;

    /**
     * Default constructor.
     */
    public AverageHPCP20F36B() {
        super("features_avghpcp20f36b", MIN_FREQUENCY, MAX_FREQUENCY, HPCP.Resolution.THIRDSEMITON, 20);
    }
}
