package org.vitrivr.cineast.core.features;

/**
 * An Extraction and Retrieval module that leverages HPCP CENS shingles according to [1].
 *
 * [1] Grosche, P., & Muller, M. (2012). Toward characteristic audio shingles for efficient cross-version music retrieval.
 *      In 2012 IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP) (pp. 473–476). IEEE. http://doi.org/10.1109/ICASSP.2012.6287919
 *
 * @author rgasser
 * @version 1.0
 * @created 26.02.17
 */
public class CENS12Shingle extends CENS {
    /* Minimum frequency to consider. */
    private final static float MIN_FREQUENCY = 50.0f;

    /* Maximum frequency to consider. */
    private final static float MAX_FREQUENCY = 5000.0f;

    /**
     * Default constructor; CENS Shingle using frequencies between 5.0f and 262.0f Hz.
     */
    public CENS12Shingle() {
        super("features_cens12", MIN_FREQUENCY, MAX_FREQUENCY);
    }
}
