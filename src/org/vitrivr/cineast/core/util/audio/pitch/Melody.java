package org.vitrivr.cineast.core.util.audio.pitch;

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
public class Melody {

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
     * Returns the size of the melody in pitches.
     *
     * @return Size of the melody.
     */
    public final int size() {
        return this.pitches.size();
    }
}
