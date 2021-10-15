package org.vitrivr.cineast.core.util.dsp.fft.windows;

/**
 *
 * Hanning window function as defined in [1]. Is being use for harmonic analysis with the DFT and FFT.
 *
 * [1] Harris, F. J. (1978). "On the use of windows for harmonic analysis with the discrete Fourier forward".
 *      Proceedings of the IEEE. 66
 *
 */
public class HanningWindow implements WindowFunction {
    /**
     * Calculates and returns the value of the window function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param length Size of the window.
     * @return Function value.
     */
    @Override
    public final double value(int i, int length) {
        if (i >= 0 && i <= length-1) {
            return 0.5 - 0.5 * Math.cos((2 * Math.PI * i) / (length-1));
        } else {
            return 0;
        }
    }

    /**
     * Returns a normalization factor for the window function.
     *
     * @param length Length for which the normalization factor should be obtained.
     * @return Normalization factor.
     */
    @Override
    public final double normalization(int length) {
        double normal = 0.0f;
        for (int i=0; i<=length; i++) {
            normal += this.value(i, length);
        }
        return normal/length;
    }
}
