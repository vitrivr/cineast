package org.vitrivr.cineast.core.util.audio.pitch;

import org.vitrivr.cineast.core.util.dsp.FrequencyUtils;
import org.vitrivr.cineast.core.util.dsp.midi.MidiUtil;

/**
 * This class represents a single pitch as returned for instance by pitch estimation classes or used by
 * pitch tracking classes. A pitch can be either created based on a frequency in Hz or a MIDI index.
 *
 */
public class Pitch {
    /** MIDI index of the pitch. */
    private final int index;

    /** Frequency of the pitch in Hz. */
    private final float frequency;

    /** Salience of the pitch. */
    private double salience;

    /** Duration of the pitch in ms. Mainly used for playback. */
    private int duration = 500;

    /**
     * Constructor of Pitch from a MIDI index.
     *
     * @param index MIDI index of the pitch.
     */
    public Pitch(int index) {
        if (index < 0) {
          throw new IllegalArgumentException("");
        }
        this.index = index;
        this.frequency = MidiUtil.midiToFrequency(this.index);
    }

    /**
     * Constructor of Pitch from a frequency.
     *
     * @param frequency Frequency of the pitch in Hz.
     */
    public Pitch(float frequency) {
        this.frequency = frequency;
        this.index = MidiUtil.frequencyToMidi(this.frequency);
    }

    /**
     * Getter for MIDI index.
     *
     * @return MIDI index of the pitch.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Getter for frequency.
     *
     * @return Frequency of the pitch.
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * Getter for pitch salience.
     *
     * @return
     */
    public double getSalience() {
        return salience;
    }

    /**
     * Setter for pitch salience.
     *
     * @param salience New value for pitch salience.
     */
    public void setSalience(double salience) {
        this.salience = salience;
    }

    /**
     * Getter for duration.
     *
     * @return Pitch salience.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Setter for duration
     *
     * @param duration New value for duration.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Calculates the distance between the current pitch and the provided in Hz.
     *
     * @param pitch Pitch to measure the distance to.
     * @return Distance in cents.
     */
    public float distanceHertz(Pitch pitch) {
        return Math.abs(this.frequency - pitch.frequency);
    }

    /**
     * Calculates the distance between the current pitch and the provided
     * pitch on a cent scale.
     *
     * @param pitch Pitch to measure the distance to.
     * @return Distance in cents.
     */
    public double distanceCents(Pitch pitch) {
        return FrequencyUtils.cents(pitch.frequency, this.frequency);
    }

    /**
     * Calculates the distance between the current pitch and the provided
     * pitch on a cent scale.
     *
     * @param frequency Frequency to measure the distance to.
     * @return Distance in cents.
     */
    public double distanceCents(float frequency) {
        return FrequencyUtils.cents(frequency, this.frequency);
    }
}
