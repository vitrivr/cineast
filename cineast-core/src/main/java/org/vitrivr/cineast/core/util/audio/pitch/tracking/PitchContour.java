package org.vitrivr.cineast.core.util.audio.pitch.tracking;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.vitrivr.cineast.core.util.audio.pitch.Pitch;

/**
 * This is a helper class for pitch tracking. It represents a pitch contour, that is, a candidate for a melody fragment. The contour has a fixed length and each slot in the sequence represents a specific timeframe (e.g. belonging to a FFT bin in the underlying STFT).
 * <p>
 * The intention behind this class is the simplification of comparison between different pitch contours, either on a frame-by-frame basis but also as an entity. In addition to the actual pitch information, the pitch contour class also provides access to pitch contour statistics related to salience and pitch frequency.
 *
 * @see PitchTracker
 */
public class PitchContour {

  /**
   * The minimum frequency in Hz on the (artifical) cent-scale.
   */
  private static final float CENT_SCALE_MINIMUM = 55.0f;

  /**
   * Entity that keeps track of salience related contour statistics.
   */
  private SummaryStatistics salienceStatistics = new SummaryStatistics();

  /**
   * Entity that keeps track of frequency related contour statistics.
   */
  private SummaryStatistics frequencyStatistics = new SummaryStatistics();

  /**
   * Sequence of pitches that form the PitchContour.
   */
  private final List<Pitch> contour = new LinkedList<>();

  /**
   * Indicates that the PitchContour statistics require recalculation.
   */
  private boolean dirty = true;

  /**
   * The start frame-index of the pitch-contour. Marks beginning in time.
   */
  private int start;

  /**
   * The end frame-index of the pitch-contour. Marks ending in time.
   */
  private int end;

  /**
   * Constructor for PitchContour.
   *
   * @param start Start-index of the contour.
   * @param pitch Pitch that belongs to the start-index.
   */
  public PitchContour(int start, Pitch pitch) {
    this.start = start;
    this.end = start;
    this.contour.add(pitch);
  }

  /**
   * Sets the pitch at the given index if the index is within the bounds of the PitchContour.
   *
   * @param p Pitch to append.
   */
  public void append(Pitch p) {
    this.contour.add(p);
    this.end += 1;
    this.dirty = true;
  }

  /**
   * Sets the pitch at the given index if the index is within the bounds of the PitchContour.
   *
   * @param p Pitch to append.
   */
  public void prepend(Pitch p) {
    this.contour.add(0, p);
    this.start -= 1;
    this.dirty = true;
  }

  /**
   * Returns the pitch at the given index or null, if the index is out of bounds. Note that even if the index is within bounds, the Pitch can still be null.
   *
   * @param i Index for which to return a pitch.
   */
  public Pitch getPitch(int i) {
    if (i >= this.start && i <= this.end) {
      return this.contour.get(i - this.start);
    } else {
      return null;
    }
  }

  /**
   * Getter for start.
   *
   * @return Start frame-index.
   */
  public final int getStart() {
    return start;
  }

  /**
   * Getter for end.
   *
   * @return End frame-index.
   */
  public final int getEnd() {
    return end;
  }


  /**
   * Size of the pitch-contour. This number also includes empty slots.
   *
   * @return Size of the contour.
   */
  public final int size() {
    return this.contour.size();
  }

  /**
   * Returns the mean of all pitches in the melody.
   *
   * @return Pitch mean
   */
  public final double pitchMean() {
    if (this.dirty) {
      this.calculate();
    }
    return this.frequencyStatistics.getMean();
  }

  /**
   * Returns the standard-deviation of all pitches in the melody.
   *
   * @return Pitch standard deviation
   */
  public final double pitchDeviation() {
    if (this.dirty) {
      this.calculate();
    }
    return this.frequencyStatistics.getStandardDeviation();
  }

  /**
   * Returns the mean-salience of all pitches in the contour.
   *
   * @return Salience mean
   */
  public final double salienceMean() {
    if (this.dirty) {
      this.calculate();
    }
    return this.salienceStatistics.getMean();
  }

  /**
   * Returns the salience standard deviation of all pitches in the contour.
   *
   * @return Salience standard deviation.
   */
  public final double salienceDeviation() {
    if (this.dirty) {
      this.calculate();
    }
    return this.salienceStatistics.getStandardDeviation();
  }

  /**
   * Returns the sum of all salience values in the pitch contour.
   */
  public final double salienceSum() {
    if (this.dirty) {
      this.calculate();
    }
    return this.salienceStatistics.getSum();
  }

  /**
   * Calculates the overlap between the given pitch-contours.
   *
   * @return Size of the overlap between two pitch-contours.
   */
  public final int overlap(PitchContour contour) {
    return Math.max(0, Math.min(this.end, contour.end) - Math.max(this.start, contour.start));
  }

  /**
   * Determines if two PitchContours overlap and returns true of false.
   *
   * @return true, if two PitchContours  overlap and falseotherwise.
   */
  public final boolean overlaps(PitchContour contour) {
    return this.overlap(contour) > 0;
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
        this.frequencyStatistics.addValue(pitch.distanceCents(CENT_SCALE_MINIMUM));
      }
    }
    this.dirty = false;
  }
}
