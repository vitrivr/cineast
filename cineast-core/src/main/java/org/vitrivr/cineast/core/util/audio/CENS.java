package org.vitrivr.cineast.core.util.audio;

import org.apache.commons.math3.util.MathArrays;
import org.vitrivr.cineast.core.util.dsp.SamplingUtilities;
import org.vitrivr.cineast.core.util.dsp.fft.windows.HanningWindow;

/**
 * This class obtains and returns CENS (Chroma Energy distribution Normalized Statistics) features according to [1]. CENS captures the temporal development of the energy distribution in the different
 * chroma or pitch-classes.
 *
 * [1] Mueller, M., Kurth, F., & Clausen, M. (2005). Audio matching via chroma-based statistical features. International Conference on Music Information Retrieval, 288–295. http://doi.org/10.1109/ASPAA.2005.1540223
 *
 *
 */
public class CENS {

    /**
     * Private constructor; this is a pure utility class
     */
    private CENS() {}

    /**
     * Calculates and returns CENS features given a Harmonic Pitch Class Profile (HPCP).
     *
     * @param hpcps HPCP from which to calculate the CENS feature.
     * @param w Size of the Hanning Window used in the convolution step. First parameter for the algorithm according to [1]
     * @param downsample Ratio for downsampling. Second parameter for the algorithm according to [1]
     *
     * @return 2D double array of CENS features.
     */
    public static double[][] cens(HPCP hpcps, int w, int downsample) {
        double[] window = (new HanningWindow()).values(w);
        double[][] cens = new double[(hpcps.size() + w)/downsample-1][hpcps.getResolution().bins];
        for (int j = 0; j<hpcps.getResolution().bins; j++) {
            double[] sequence = new double[hpcps.size()];
            for (int i = 0; i<hpcps.size(); i++) {
                sequence[i] = mapToCens(hpcps.getSumNormalizedHpcp(i)[j]);
            }
            double[] result = SamplingUtilities.downsample(MathArrays.convolve(sequence, window), downsample);
            for (int n = 0; n<result.length; n++) {
                cens[n][j] = result[n];
            }
        }
        return cens;
    }

    /**
     * Maps normalized energy values to the logarithmic CENS energy scale.
     *
     * @param value Energy value that should be mapped.
     * @return Value on the CENS scale that corresponds to the provided value.
     */
    private static float mapToCens(float value) {
        if (value >= 0.4f && value <= 1.0f) {
            return 4.0f;
        } else if (value >= 0.2f && value < 0.4f) {
            return 3.0f;
        } else if (value >= 0.1f && value < 0.2f) {
            return 2.0f;
        } else if (value >= 0.05f && value < 0.1f) {
            return 1.0f;
        } else {
            return 0.0f;
        }
    }
}
