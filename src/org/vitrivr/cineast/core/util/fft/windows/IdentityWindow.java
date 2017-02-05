package org.vitrivr.cineast.core.util.fft.windows;

/**
 * IdentityWindows - this is the same as applying no window at all.
 *
 * @author rgasser
 * @version 1.0
 * @created 02.02.17
 */
public class IdentityWindow implements WindowFunction {
    /**
     * Returns the value of the windowing function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param size Size of the window.
     * @return Function value.
     */
    @Override
    public final double value(int i, int size) {
        return 1;
    }

    /**
     * Returns a normalization factor for the window function.
     *
     * @param length Length for which the normalization factor should be obtained.
     * @return Normalization factor.
     */
    public final double normalization(int length) {
        return 1.0f;
    }
}
