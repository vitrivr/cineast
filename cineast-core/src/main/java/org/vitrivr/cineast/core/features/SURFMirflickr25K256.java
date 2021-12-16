package org.vitrivr.cineast.core.features;

/**
 * A Extraction and Retrieval module that uses SURF descriptors and a 256 word codebook based on Mirflickr 25K to obtain a
 * histograms of codewords. These histograms ares used as feature-vectors.
 *
 */
public class SURFMirflickr25K256 extends SURF {
    /**
     * Default constructor.
     */
    public SURFMirflickr25K256() {
        super("features_surfmf25k256", 256);
    }

    /**
     * Returns the full name of the Codebook to use.
     */
    @Override
    protected String codebook() {
        return "mirflickr25k-256.surfcb";
    }
}
