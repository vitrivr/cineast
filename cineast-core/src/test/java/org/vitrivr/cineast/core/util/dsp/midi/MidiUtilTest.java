package org.vitrivr.cineast.core.util.dsp.midi;

import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.util.dsp.midi.MidiUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MidiUtilTest {
    /**
     * Array holding reference pitches from MIDI 0 to MIDI 127. Code from
     * http://subsynth.sourceforge.net/midinote2freq.html
     */
    private static final float[] PITCHES;
    static {
        PITCHES = new float[127];
        double a = MidiUtil.F_REF; // a is 440 hz...
        for (int i = 0; i < 127; ++i) {
            PITCHES[i] = (float)((a / 32.0) * Math.pow(2,((i - 9.0)/12.0)));
        }
    }

    /**
     * Tests the frequency to MIDI index conversion methd.
     */
    @Test
    public void testFrequencyToMidi() {
        for (int i=0; i<PITCHES.length; i++) {
            assertEquals(i, MidiUtil.frequencyToMidi(PITCHES[i]), "MIDI index does not match the expected MIDI index.");
        }
    }

    /**
     * Tests the MIDI-Index to frequency conversion method.
     */
    @Test
    public void testMidiToFrequency() {
        for (int m=0; m<127; m++) {
            assertEquals(PITCHES[m], MidiUtil.midiToFrequency(m), 1e-8, "Calculated frequency does not match the expected frequency.");
        }
    }
}
