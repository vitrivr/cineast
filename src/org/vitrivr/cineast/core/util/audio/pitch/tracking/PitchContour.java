package org.vitrivr.cineast.core.util.audio.pitch.tracking;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.util.audio.pitch.Pitch;

/**
 * This is a helper class for pitch tracking. It represents a pitch contour, that is, a candidate for a melody fragment. The contour
 * has a fixed length and each slot in the sequence represents a specific timeframe (e.g. belonging to a FFT bin in the underlying STFT).
 *
 * The intention behind this class is the simplification of comparison between different pitch contours, either on a frame-by-frame basis
 * but also as an entity. In addition to the actual pitch information, the pitch contour class also provides access to pitch contour statistics
 * related to salience and pitch frequency.
 *
 * @author rgasser
 * @version 1.0
 * @created 19.04.17
 */
public class PitchContour {
    /** Entity that keeps track of salience related contour statistics. */
    private SummaryStatistics salienceStatistics = new SummaryStatistics();

    /** Entity that keeps track of frequency related contour statistics. */
    private SummaryStatistics frequencyStatistics = new SummaryStatistics();

    /** Sequence of pitches that form the PitchContour. */
    private final Pitch[] contour;

    /** Indicates that the PitchContour statistics require recalculation. */
    private boolean dirty = false;

    /**
     * Constructor for the PitchContour class.
     *
     * @param size Size of the contour, i.e. number of pitches.
     */
    public PitchContour(int size) {
        this.contour = new Pitch[size];
    }

    /**
     * Sets the pitch at the given index if the index is within the bounds
     * of the PitchContour.
     *
     * @param i Index for which to return a pitch.
     * @param p Pitch to set.
     */
    public void setPitch(int i, Pitch p) {
        if (i < this.contour.length) {
            this.contour[i] = p;
            this.dirty = true;
        }
    }

    /**
     * Returns the pitch at the given index or null, if the index is out of bounds.
     * Note that even if the index is within bounds, the Pitch can still be null.
     *
     * @param i Index for which to return a pitch.
     */
    public Pitch getPitch(int i) {
        if (i < this.contour.length) {
            return this.contour[i];
        } else {
            return null;
        }
    }

    /**
     * Size of the pitch-contour. This number also includes
     * empty slots.
     *
     * @return Size of the contour.
     */
    public int size() {
        return this.contour.length;
    }

    /**
     * Returns the mean of all pitches in the melody.
     *
     * @return Pitch mean
     */
    public double pitchMean() {
        if (this.dirty) this.calculate();
        return this.frequencyStatistics.getMean();
    }

    /**
     * Returns the standard-deviation of all pitches in the melody.
     *
     * @return Pitch standard deviation
     */
    public double pitchDeviation() {
        if (this.dirty) this.calculate();
        return this.frequencyStatistics.getStandardDeviation();
    }

    /**
     * Returns the standard-deviation of all pitches in the melody in cents.
     *
     * @return Pitch standard deviation
     */
    public double pitchDeviationCents() {
        double mean = this.pitchMean();
        double std = this.pitchDeviation();
        return 1200*Math.log((mean+std)/mean)/Math.log(2);
    }

    /**
     * Returns the mean-salience of all pitches in the contour.
     *
     * @return Salience mean
     */
    public double salienceMean() {
        if (this.dirty) this.calculate();
        return this.salienceStatistics.getMean();
    }

    /**
     * Returns the salience standard deviation of all pitches in the contour.
     *
     * @return Salience standard deviation.
     */
    public double salienceDeviation() {
        if (this.dirty) this.calculate();
        return this.salienceStatistics.getStandardDeviation();
    }

    /**
     * Returns the sum of all salience values in the pitch contour.
     *
     * @return
     */
    public double salienceSum() {
        if (this.dirty) this.calculate();
        return this.salienceStatistics.getSum();
    }

    /**
     * Re-calculates the PitchContour statistics.
     */
    private void calculate() {
        this.salienceStatistics.clear();
        this.frequencyStatistics.clear();
        for (Pitch pitch : this.contour) {
            if (pitch != null) {
                this.salienceStatistics.addValue(pitch.getSalience());
                this.frequencyStatistics.addValue(pitch.getFrequency());
            }
        }
        this.dirty = false;
    }
}
