package org.vitrivr.cineast.core.util.dsp.fft.windows;

/**
 * IdentityWindows - this is the same as applying no window at all.
 *
 * @author rgasser
 * @version 1.0
 * @created 02.02.17
 */
public class RectangularWindow implements WindowFunction {
    /**
     * Returns the value of the windowing function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param length Size of the window.
     * @return Function value.
     */
    @Override
    public final double value(int i, int length) {
        if (i >= 0 && i <= length-1) {
            return 1.0;
        } else {
            return 0.0;
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
        return 1.0;
    }
}
