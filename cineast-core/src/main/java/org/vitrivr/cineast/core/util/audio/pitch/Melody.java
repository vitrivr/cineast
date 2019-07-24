package org.vitrivr.cineast.core.util.audio.pitch;

import org.vitrivr.cineast.core.util.dsp.FrequencyUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a melody as as sequence of pitches.
 *
 * @author rgasser
 * @version 1.0
 * @created 19.04.17
 */
public class Melody implements Iterable<Pitch> {
    /** List holding the pitches that comprise this melody. */
    private final List<Pitch> pitches = new LinkedList<>();

    /**
     * Appends a pitch to the melody stream.
     *
     * @param pitch Pitch to append.
     */
    public void append(Pitch pitch) {
        this.pitches.add(pitch);
    }

    /**
     * Prepends a pitch to the melody stream.
     *
     * @param pitch Pitch to prepend.
     */
    public void prepend(Pitch pitch) {
        this.pitches.add(0, pitch);
    }

    /**
     * Accessor for a pitch at a specific index.
     *
     * @param index Index of the pitch.
     * @return Pitch
     */
    public Pitch getPitch(int index) {
        return this.pitches.get(index);
    }

    /**
     * Returns the first pitch in the melody.
     *
     * @return First pitch in the melody.
     */
    public final Pitch first() {
        if (this.pitches.size() > 0) {
            return this.pitches.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the last pitch in the melody.
     *
     * @return Last pitch in the melody.
     */
    public final Pitch last() {
        if (this.pitches.size() > 0) {
            return this.pitches.get(this.pitches.size()-1);
        } else {
            return null;
        }
    }

    /**
     * Returns a list of MIDI indices corresponding to the pitches in the melody.
     *
     * @return List of midi indices.
     */
    public List<Integer> getMidiIndices() {
        return this.pitches.stream().map(Pitch::getIndex).collect(Collectors.toList());
    }

    /**
     * Returns a list of frequencies for every pitch.
     *
     * @return List of pitch-frequencies.
     */
    public List<Float> getFrequencies() {
        return this.pitches.stream().map(Pitch::getFrequency).collect(Collectors.toList());
    }

    /**
     * Returns the distance in cents of every pitch to some arbitrary minimum.
     *
     * @param min The minimum frequency to calculate the distance from in Hz.
     * @return List of pitch-distances in cents.
     */
    public List<Double> getCentDistances(float min) {
        return this.pitches.stream().map(p -> FrequencyUtils.cents(p.getFrequency(), min)).collect(Collectors.toList());
    }

    /**
     * Returns the size of the melody in pitches.
     *
     * @return Size of the melody.
     */
    public final int size() {
        return this.pitches.size();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Pitch> iterator() {
        return this.pitches.iterator();
    }
}
