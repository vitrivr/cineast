package org.vitrivr.cineast.core.util.dsp.midi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.audio.pitch.Melody;
import org.vitrivr.cineast.core.util.audio.pitch.Pitch;

import javax.sound.midi.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.04.17
 */
public final class MidiUtil {

    private static final Logger LOGGER = LogManager.getLogger();


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

    /**
     * Convenience methods that allows playback of a melody on a MIDI channel.
     *
     * @param melody Melody that should be played.
     * @param instrument Index of the instrument to be used for playback.
     * @param channel Channel to play the melody on.
     */
    public static void play(Melody melody, int instrument, int channel) {
        try {
            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();
            Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
            MidiChannel[] mChannels = midiSynth.getChannels();

            midiSynth.loadInstrument(instr[instrument]);

            for (Pitch pitch : melody) {
                mChannels[channel].noteOn(pitch.getIndex(), 100);
                try {
                    Thread.sleep(pitch.getDuration());
                } catch( InterruptedException e ) {
                    LOGGER.error("Thread was interrupted during playback ({}).", LogHelper.getStackTrace(e));
                    return;
                }
                mChannels[0].noteOff(pitch.getIndex());
            }
        } catch (MidiUnavailableException e) {
            LOGGER.error("MIDI is not available ({}).", LogHelper.getStackTrace(e));
        }
    }
}
