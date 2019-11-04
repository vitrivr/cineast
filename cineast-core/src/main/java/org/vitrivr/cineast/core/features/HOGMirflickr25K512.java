package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.util.MathHelper;

/**
 * A Extraction and Retrieval module that uses HOG descriptors and a 512 word codebook based on Mirflickr 25K to obtain a
 * histograms of codewords. These histograms ares used as feature-vectors.
 *
 * @author rgasser
 * @version 1.0
 * @created 18.01.17
 */
public class HOGMirflickr25K512 extends HOG {
    /**
     * Constructor: defines name of entity and max distance.
     */
    public HOGMirflickr25K512() {
        super("features_hogmf25k512", (float)MathHelper.SQRT2/4.0f, 512);
    }

    /**
     * Returns the full name of the codebook to use. All codebooks must be placed in the
     * ./resources/codebooks folder.
     *
     * @return
     */
    @Override
    protected String codebook() {
        return "mirflickr25k-512.hogcb";
    }
}
