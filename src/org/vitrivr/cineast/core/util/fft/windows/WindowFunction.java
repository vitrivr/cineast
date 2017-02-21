package org.vitrivr.cineast.core.util.fft.windows;

/**
 * Interface implemented by any windows function (e.g. used for FFT).
 *
 * @author rgasser
 * @version 1.0
 * @created 02.02.17
 */
public interface WindowFunction {

    /**
     * Calculates and returns the value of the window function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param length Size of the window.
     * @return Function value.
     */
    double value(int i, int length);


    /**
     * Calculates and returns a normalization factor for the window function.
     *
     * @param length Length for which the normalization factor should be obtained.
     * @return Normalization factor.
     */
    default double normalization(int length) {
        double normal = 0.0f;
        for (int i=0; i<=length; i++) {
            normal += this.value(i, length);
        }
        return normal/length;
    }
}
