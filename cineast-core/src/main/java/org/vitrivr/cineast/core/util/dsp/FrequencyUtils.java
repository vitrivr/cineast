package org.vitrivr.cineast.core.util.dsp;


public final class FrequencyUtils {

    /** */
    private FrequencyUtils() {}


    /** One octave distance in cents. */
    public final static float OCTAVE_CENT = 1200.0f;

    /**
     * Returns the distance between two frequencies in Hz on a Cent scale. If the returned
     * value is < 0 then f2 < f1, otherwise f2 > f1.
     *
     * @param f1 First frequency in Hz.
     * @param f2 Second frequency in Hz.
     * @return Distance in Cents.
     */
    public static double cents(float f1, float f2) {
        return 1200.0f * (Math.log(f2/f1)/Math.log(2));
    }
}
