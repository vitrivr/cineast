package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.util.audio.HPCP;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.05.17
 */
public class HPCP12Shingle extends HPCPShingle {
    private final static float MIN_FREQUENCY = 50.0f;

    private final static float MAX_FREQUENCY = 5000.0f;

    /**
     * Default constructor; HPCP Shingle with 12 bins, using frequencies between 50.0f and 5000.0f Hz.
     */
    public HPCP12Shingle() {
        super("features_hpcp12shingle", MIN_FREQUENCY, MAX_FREQUENCY, HPCP.Resolution.FULLSEMITONE);
    }
}
