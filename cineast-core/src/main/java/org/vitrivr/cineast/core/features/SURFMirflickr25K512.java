package org.vitrivr.cineast.core.features;

/**
 * A Extraction and Retrieval module that uses SURF descriptors and a 512 word codebook based on Mirflickr 25K to obtain a
 * histograms of codewords. These histograms ares used as feature-vectors.
 *
 */
public class SURFMirflickr25K512 extends SURF {
    /**
     * Default constructor.
     */
    public SURFMirflickr25K512() {
        super("features_surfmf25k512", 512);
    }

    /**
     * Returns the full name of the Codebook to use.
     *
     * @return
     */
    @Override
    protected String codebook() {
        return "mirflickr25k-512.surfcb";
    }
}
