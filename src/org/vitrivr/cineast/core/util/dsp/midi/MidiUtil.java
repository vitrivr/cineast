package org.vitrivr.cineast.core.util.dsp.midi;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.04.17
 */
public final class MidiUtil {

    /** Reference frequency in Hz. Corresponds to the musical note A (A440 or A4) above the middle C. */
    public static final double F_REF = 440.0f;

    /**
     * Private constructor; class cannot be instantiated.
     */
    private MidiUtil() {}

    /**
     * Converts the given frequency to the corresponding MIDI index.
     *
     * @param frequency Frequency to convert.
     * @return MIDI index associated with the frequency.
     */
    public static int frequencyToMidi(float frequency) {
        return 69 + (int)Math.round(12 * (Math.log(frequency/F_REF)/Math.log(2.0)));
    }

    /**
     * Converts the given midi-index to the corresponding frequency.
     *
     * @param m Index to convert.
     * @return Frequency associated with the MIDI index.
     */
    public static float midiToFrequency(int m) {
        return (float)(Math.pow(2,(m-69.0)/12.0)*F_REF);
    }

}
