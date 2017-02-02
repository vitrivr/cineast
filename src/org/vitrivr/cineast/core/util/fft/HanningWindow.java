package org.vitrivr.cineast.core.util.fft;

/**
 *
 * Hanning window function as defined in [1]. Is being use for harmonic analysis with the DFT and FFT.
 *
 * [1] Harris, F. J. (1978). "On the use of windows for harmonic analysis with the discrete Fourier transform".
 *      Proceedings of the IEEE. 66
 *
 * @author rgasser
 * @version 1.0
 * @created 02.02.17
 */
public class HanningWindow implements WindowFunction {
    /**
     * Calculates and returns the value of the window function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param size Size of the window.
     * @return Function value.
     */
    @Override
    public final double value(int i, int size) {
        return 0.5 - 0.5 * Math.cos(2 * Math.PI * i / size);
    }

    /**
     * Returns a normalization factor for the window function.
     *
     * @param length Length for which the normalization factor should be obtained.
     * @return Normalization factor.
     */
    public final double normalization(int length) {
        return 0.375f;
    }
}
