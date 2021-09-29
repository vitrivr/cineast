package org.vitrivr.cineast.core.util.dsp.fft.windows;


public class BlackmanHarrisWindow implements WindowFunction {

    /** Constants defined for the Blackman-Harris Window. */
    private static final float a0 = 0.35875f;
    private static final float a1 = 0.48829f;
    private static final float a2 = 0.14128f;
    private static final float a3 = 0.01168f;

    /**
     * Calculates and returns the value of the Blackman-Harris window function at position i.
     *
     * @param i The position for which the function value should be calculated.
     * @param length Size of the window.
     * @return Function value.
     */
    @Override
    public double value(int i, int length) {
        if (i >= 0 && i <= length-1) {
            return a0 - a1 * Math.cos((2 * Math.PI * i) / (length - 1)) + a2 * Math.cos((4 * Math.PI * i) / (length - 1)) - a3 * Math.cos((6 * Math.PI * i) / (length - 1));
        } else {
            return 0.0;
        }
    }
}
