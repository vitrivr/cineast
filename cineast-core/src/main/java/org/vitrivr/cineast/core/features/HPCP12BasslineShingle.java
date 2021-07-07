package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.util.audio.HPCP;

/**
 * An Extraction and Retrieval module that leverages pure HPCP shingles according to [1]. The particular module
 * focuses on bassline frequencies according to an idea found in [2].
 *
 * [1] Casey, M., Rhodes, C., & Slaney, M. (2008). Analysis of minimum distances in high-dimensional musical spaces.
 *      IEEE Transactions on Audio, Speech and Language Processing, 16(5), 1015–1028. http://doi.org/10.1109/TASL.2008.925883
 *
 * [2] Salamon, J., Serrà, J., & Gómez, E. (2012). Tonal representations for music retrieval: from version identification to query-by-humming.
 *    International Journal of Multimedia Information Retrieval, 2(1), 45–58. http://doi.org/10.1007/s13735-012-0026-0
 *
 */
public class HPCP12BasslineShingle extends HPCPShingle {

    private final static float MIN_FREQUENCY = 22.0f;

    private final static float MAX_FREQUENCY = 262.0f;

    /**
     * Default constructor; HPCP Shingle with 12 bins, using frequencies between 5.0f and 262.0f Hz.
     */
    public HPCP12BasslineShingle() {
        super("features_hpcp12bassline", MIN_FREQUENCY, MAX_FREQUENCY, HPCP.Resolution.FULLSEMITONE);
    }
}
