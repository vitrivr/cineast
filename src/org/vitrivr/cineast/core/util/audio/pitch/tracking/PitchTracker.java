package org.vitrivr.cineast.core.util.audio.pitch.tracking;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import org.vitrivr.cineast.core.util.audio.pitch.Melody;
import org.vitrivr.cineast.core.util.audio.pitch.Pitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the pitch-tracking algorithm described in [1]. It can be used to extract a melody from a
 * list of pitch-candidates. The list of pitch-candidates must be extracted using a pitch-extraction method.
 *
 * [1] Salamon, J., & Gomez, E. (2012). Melody extraction from polyphonic music signals using pitch contour characteristics.
 *      IEEE Transactions on Audio, Speech and Language Processing, 20(6), 1759–1770. http://doi.org/10.1109/TASL.2012.2188515
 *
 * @author rgasser
 * @version 1.0
 * @created 19.04.17
 */
public class PitchTracker {
    /** Pitch-candidates that have been ruled out. */
    private Pitch[][] s0;

    /** Active pitch-candidates. */
    private Pitch[][] s1;

    /** Threshold in first (per-frame) filter step. */
    private final float t1;

    /** Threshold in second (global) filter step. */
    private final float t2;

    /** Voicing threshold according to [1]. */
    private final float v_threshold;

    /** Maximum distance between two pitches in subsequent frames (in cents) according to [1]. */
    private final float d_max;

    /** Maximum amount of misses before contour tracking is aborted. */
    private final int m_max;

    /** SummaryStatistics for the PitchContours. */
    private final SummaryStatistics contourStatistics = new SummaryStatistics();

    /** List of PitchContours. This list is generated int the pitch-tracking step.*/
    private final List<PitchContour> pitchContours = new ArrayList<>();

    /**
     * Default constructor for PitchTracker. Uses the settings described in [1].
     */
    public PitchTracker() {
        this(0.9f, 0.9f,0.2f, 80.0f, 3);
    }

    /**
     * Constructor for PitchTracker class.
     *
     * @param t1 Threshold in first (per-frame) filter step.
     * @param t2 Threshold in second (global) filter step.
     * @param v_threshold Threshold for voicing detection.
     * @param d_max Maximum distance between two pitches in subsequent frames during tracking.
     * @param m_max Maximum amount of misses before contour tracking is aborted.
     */
    public PitchTracker(float t1, float t2, float v_threshold, float d_max, int m_max) {
        this.t1 = t1;
        this.t2 = t2;
        this.v_threshold = v_threshold;
        this.d_max = d_max;
        this.m_max = m_max;
    }

    /**
     * Initializes the PitchTracker with a set of new pitch candidates. Prepares everything
     * for pitch-streaming and subsequent melody selection and discards previous results.
     *
     * @param candidates Nested list of pitch candidates. The first list contains one list per time-frame, each time-frame then contains one to n pitch-candidates.
     */
    public void initialize(List<List<Pitch>> candidates) {
        /* Initialize S1 and S0 with new pitch-candidates */
        this.s1 = new Pitch[candidates.size()][];
        this.s0 = new Pitch[candidates.size()][];
        for (int i=0; i<candidates.size();i++) {
            int size = candidates.get(i).size();
            this.s1[i] = new Pitch[size];
            this.s0[i] = new Pitch[size];
            for (int j=0; j<size; j++) {
                this.s1[i][j] = candidates.get(i).get(j);
            }
        }

        /* Clear pre-calculates statistics and contours. */
        this.contourStatistics.clear();
        this.pitchContours.clear();
    }

    /**
     *  Executes the pitch-tracking / pitch-streaming step
     *
     * @return
     */
    public void trackPitches() {
        /* Apply the two filter stages described in [1], section II-B. */
        if (this.t1 > 0) this.applyPerFrameFilter();
        if (this.t2 > 0) this.applyGlobalFilter();

        int[] currentPointer = null;
        Pitch currentPitch = null;
        while (true) {
            /* Prepares a new pitch contour. */
            final PitchContour contour = new PitchContour(this.s1.length);

            /* Find the pitch with the maximum salience in S1 and add it to the melody. */
            currentPointer = this.seekGlobalMaxInS1();
            if (currentPointer[0] == -1 || currentPointer[1] == -1) break;

            /* Select that pitch from S1 and add it to the contour. */
            currentPitch = this.selectFromS1(currentPointer[0], currentPointer[1]);
            contour.setPitch(currentPointer[0], currentPitch);

            /* Tracks pitch contour upstream. */
            this.trackUpstream(contour, currentPointer[0]);

            /* Track pitch contour downstream. */
            this.trackDownstream(contour, currentPointer[0]);

            /* Add contour to list of contours */
            this.addContour(contour);
        }

        /*
         * Sort the list of pitch-contours so that the PitchContour with the highest salience-sum
         * is the first entry.
         */
        this.pitchContours.sort(Comparator.comparingDouble(PitchContour::salienceSum));
        Collections.reverse( this.pitchContours);
    }

    /**
     *
     * @return
     */
    public Melody extractMelody(int iterations) {
        /* Return if no pitch-contours are available. */
        if (this.pitchContours.size() == 0) return null;

        /* Copies the pitch-contours from the tracking step. */
        List<PitchContour> workingList = new ArrayList<>(this.pitchContours);

        /* Filters the workingList and removes unvoiced pitch-contours. */
        this.voicingDetection(workingList);

        /*
         * Use the mean-contour to remove octave-duplicates and pitch-outliers. Perform multiple
         * iterations in which the mean-contour is updated.
         */
        Pitch[] mean = this.meanContour(this.pitchContours);
        List<PitchContour> copy = null;
        for (int i=0; i<iterations; i++) {
            copy = new ArrayList<>(workingList);
            this.detectAndRemoveOctaveDuplicates(copy, mean);
            mean = this.meanContour(copy);
            this.detectAndRemovePitchOutliers(copy, mean);
            mean = this.meanContour(copy);
        }

        /*
         * Sort the copied-list so that the PitchContour with the highest salience-sum is
         * the first entry.
         */
        copy.sort(Comparator.comparingDouble(PitchContour::salienceSum));
        Collections.reverse(workingList);


        /* Construct melody from remaining pitch-contours. */
        Melody melody = new Melody();
        for (int i=0;i<this.s1.length;i++) {
            boolean hit = false;
            for (PitchContour contour : workingList) {
                if (contour.getPitch(i) != null) {
                    melody.append(contour.getPitch(i));
                    hit = true;
                    break;
                }
            }

            /* if (!hit) {
                for (PitchContour contour : this.pitchContours) {
                    if (contour.getPitch(i) != null) {
                        melody.append(contour.getPitch(i));
                        break;
                    }
                }
            } */
        }
        return melody;
    }

    /**
     *
     * @param contours
     * @return
     */
    public Pitch[] meanContour(List<PitchContour> contours) {

        final int size = 20;
        double[] pitchmeans = new double[this.s1.length];
        Pitch[] meanpitches = new Pitch[this.s1.length];

        for (int i=0; i<this.s1.length; i++) {
            float sum = 0;
            float mean = 0.0f;
            for (PitchContour contour : contours) {
                if (contour.getPitch(i) != null) {
                    mean += contour.getPitch(i).getFrequency() * contour.salienceSum();
                    sum += contour.salienceSum();
                }
            }

            if (sum > 0) {
                pitchmeans[i] = mean/sum;
            }
        }

        for (int i=0; i<pitchmeans.length; i++) {
            double mean = 0.0f;
            for (int s=0; s<size; s++) {
                if (i+s < pitchmeans.length) {
                    mean += pitchmeans[i+s];
                }
            }

            pitchmeans[i] = mean/size;
        }

        for (int i=0; i<pitchmeans.length; i++) {
            meanpitches[i] = new Pitch((float)pitchmeans[i]);
        }

        return meanpitches;
    }




    /**
     * Selects the pitch specified by the two pitch-indices from S1 a and returns
     * it, if it exists. Thereby, the pitch is removed from S1.
     *
     * @param t Temporal index of the pitch candidate.
     * @param i Position of the pitch candidate
     */
    private Pitch selectFromS1(int t, int i) {
        Pitch pitch = this.s1[t][i];
        if (pitch != null) {
            this.s1[t][i] = null;
            return pitch;
        } else {
            return null;
        }
    }

    /**
     * Selects the pitch specified by the two pitch-indices from S0 a and returns
     * it, if it exists. Thereby, the pitch is removed from S0.
     *
     * @param t Temporal index of the pitch candidate.
     * @param i Position of the pitch candidate
     */
    private Pitch selectFromS0(int t, int i) {
        Pitch pitch = this.s0[t][i];
        if (pitch != null) {
            this.s0[t][i] = null;
            return pitch;
        }
        return null;
    }

    /**
     * Moves a pitch-candidate from S1 to S0.
     *
     * @param t Temporal index of the pitch candidate.
     * @param i Position of the pitch candidate
     * @return true if pitch was moved and false otherwise.
     */
    private boolean moveToS0(int t, int i) {
        if (this.s1[t][i] != null) {
            this.s0[t][i] = this.s1[t][i];
            this.s1[t][i] = null;
            return true;
        } else {
            return false;
        }
    }


    /**
     * Seeks the pitch with maximum salience in S1 and returns an int array that contains the
     * indexes which point to that maximum. If no maximum was found, {-1, -1} is returned.
     *
     * @return Indexes {t,i} pointing to maximum is S1.
     */
    private int[] seekGlobalMaxInS1() {
        int[] max = {-1, -1};
        for (int t = 0; t<this.s1.length; t++) {
            int max_i = this.seekFrameMaxInS1(t);
            if (max_i == -1) continue;
            if (max[0] == -1 || max[1] == -1 || this.s1[t][max_i].getSalience() > this.s1[max[0]][max[1]].getSalience()) {
                max[0] = t;
                max[1] = max_i;
            }
        }
        return max;
    }

    /**
     * Seeks the pitch with the maximum salience in the specified frame in S1 and returns the
     * index i that point to that maximum. If now maximum was found, -1 is returned.
     *
     * @param t Temporal index of the frame in S1.
     * @return Index of the maximum in the specified frame.
     */
    private int seekFrameMaxInS1(int t) {
        Pitch[] pitches = this.s1[t];
        int max = -1;
        for (int i=0;i<pitches.length;i++) {
            if (pitches[i] == null) continue;
            if (max == -1 || pitches[i].getSalience() < pitches[max].getSalience()) {
                max = i;
            }
        }
        return max;
    }

    /**
     * Adds a pitch-contour to the list of contours and updates the contour-statistics.
     *
     * @param contour PitchContour to add.
     */
    private void addContour(PitchContour contour) {
        this.pitchContours.add(contour);
        this.contourStatistics.addValue(contour.salienceMean());
    }

    /**
     * Applies a per-frame filter on pitches in S1 and moves all peaks whose salience is bellow a certain
     * threshold from S1 to S0. This filter is described in [1], section II-C.
     */
    private void applyPerFrameFilter() {
        for (int t = 0; t<this.s1.length; t++) {
            int max_idx = this.seekFrameMaxInS1(t);
            if (max_idx == -1) continue;
            int size = this.s1[t].length;
            for (int i=0; i<size; i++) {
                if (this.s1[t][i] == null) continue;
                if (this.s1[t][i].getSalience() < this.t1 * this.s1[t][max_idx].getSalience()) {
                    this.moveToS0(t,i);
                }
            }
        }
    }

    /**
     * Applies a global filter on pitches in S1 and moves all pitches whose salience is bellow a certain
     * threshold from S1 to S0. This filter is described in [1], section II-C.
     */
    private void applyGlobalFilter() {
        SummaryStatistics statistics = new SummaryStatistics();

        /* Iteration #1: Gather data to obtain salience statistics. */
        for (int t=0; t<this.s1.length; t++) {
            for (int i=0; i<this.s1[t].length; i++) {
                if (this.s1[t][i] == null) continue;
                statistics.addValue(this.s1[t][i].getSalience());
            }
        }

        /* Iteration #2: Move pitches that are bellow the threshold. */
        for (int t=0; t<this.s1.length; t++) {
            for (int i=0; i<this.s1[t].length; i++) {
                if (this.s1[t][i] == null) continue;
                if (this.s1[t][i].getSalience() < statistics.getMean() - this.t2 * statistics.getStandardDeviation()) {
                    this.moveToS0(t,i);
                }
            }
        }
    }

    /**
     *
     * @param contour
     * @param start
     * @return
     */
    private void trackUpstream(final PitchContour contour, int start) {
        int misses = 0;
        int frameindex = start;

        Pitch lastPitch = contour.getPitch(frameindex);

        /* */
        while (frameindex < this.s1.length-1) {
            frameindex += 1;
            boolean found = false;

            /* Search for a matching pitch candidate in S1 in the next frame. */
            for (int j=0;j<this.s1[frameindex].length; j++) {
                if (this.s1[frameindex][j] == null) continue;
                if (Math.abs(this.s1[frameindex][j].distanceCents(lastPitch)) <= this.d_max) {
                    lastPitch = this.selectFromS1(frameindex,j);
                    contour.setPitch(frameindex, lastPitch);
                    found = true;
                    misses = 0;
                    break;
                }
            }

            /* If a pitch candidate was found in S1, continue to next iteration. */
            if (found) continue;

            /* This code only gets executed if no matching pitch could be found in S1:
             *
             * Increase number of misses and make sure, that it does not exceed m_max (otherwise return false).
             * Search for pitch candidates in S0 afterwards.
             */
            misses += 1;
            if (misses >= this.m_max) break;
            for (int j=0;j<this.s0[frameindex].length; j++) {
                if (this.s0[frameindex][j] == null) continue;
                if (Math.abs(this.s0[frameindex][j].distanceCents(lastPitch)) <= this.d_max) {
                    lastPitch = this.selectFromS0(frameindex,j);
                    contour.setPitch(frameindex, lastPitch);

                    break;
                }
            }
        }
    }

    /**
     *
     * @param contour
     * @param start
     * @return
     */
    private void trackDownstream(final PitchContour contour, int start) {
        int misses = 0;
        int frameindex = start;
        Pitch lastPitch = contour.getPitch(frameindex);
        /* */
        while (frameindex > 1) {
            frameindex -= 1;
            boolean found = false;

            /* Search for a matching pitch candidate in S1 in the next frame. */
            for (int j=0;j<this.s1[frameindex].length; j++) {
                if (this.s1[frameindex][j] == null) continue;
                if (Math.abs(this.s1[frameindex][j].distanceCents(lastPitch)) < this.d_max) {
                    lastPitch = this.selectFromS1(frameindex,j);
                    contour.setPitch(frameindex, lastPitch);
                    found = true;
                    misses = 0;
                    break;
                }
            }

            /* If a pitch candidate was found in S1, continue to next iteration. */
            if (found) continue;

            /* This code only gets executed if no matching pitch could be found in S1:
             *
             * Increase number of misses and make sure, that it does not exceed m_max (otherwise return false).
             * Search for pitch candidates in S0 afterwards.
             */
            misses += 1;
            if (misses >= this.m_max) break;
            for (int j=0;j<this.s0[frameindex].length; j++) {
                if (this.s0[frameindex][j] == null) continue;
                if (Math.abs(this.s0[frameindex][j].distanceCents(lastPitch)) < this.d_max) {
                    lastPitch = this.selectFromS0(frameindex,j);
                    contour.setPitch(frameindex, lastPitch);
                    break;
                }
            }
        }
    }

    /**
     *
     */
    private void voicingDetection(List<PitchContour> contours) {
        contours.removeIf(c -> {
            if (c.pitchDeviationCents() < 40.0f) {
               return c.salienceMean() < contourStatistics.getMean() - this.v_threshold * contourStatistics.getStandardDeviation();
            } else {
                return false;
            }
        });
    }

    /**
     *
     * @param contours
     */
    private void detectAndRemoveOctaveDuplicates(List<PitchContour> contours, Pitch[] meanpitches) {
        contours.removeIf(ct -> {
            for (PitchContour ci : contours) {
                /* Take contour at i; if c == ci then skip. */
                if (ct == ci) continue;

                /* Calculate mean distance from ct to ci. */
                double distance = 0.0;
                int count = 0;
                for (int k = 0; k < ci.size(); k++) {
                    if (ci.getPitch(k) != null && ct.getPitch(k) != null) {
                        distance += Math.abs(ci.getPitch(k).distanceCents(ct.getPitch(k)));
                        count += 1;
                    }
                }

                /* Count < 5 means that the two contours have very little overlap. */
                if (count < 5) continue;

                /* Normalise distance.. */
                distance /= count;

                /* If distance is between 1150.0 and 1250 cents, the an octave duplicate has been detected! */
                if (distance >= 1150.0f && distance <= 1250.0f) {
                    double di = 0.0f;
                    double dt = 0.0f;
                    int counti = 0;
                    int countt = 0;
                    for (int k = 0; k < ci.size(); k++) {
                        if (ci.getPitch(k) != null) {
                            di += Math.abs(ci.getPitch(k).distanceCents(meanpitches[k]));
                            counti += 1;
                        }
                        if (ct.getPitch(k) != null) {
                            dt += Math.abs(ct.getPitch(k).distanceCents(meanpitches[k]));
                            countt += 1;
                        }
                    }

                    /* Normalise distances. */
                    di /= counti;
                    dt /= countt;

                    /* If distance dt > di, then remove ct, */
                    if (dt > di) return true;
                }
            }
            return false;
        });
    }

    /**
     *
     * @param contours
     * @param meanpitches
     */
    private void detectAndRemovePitchOutliers(List<PitchContour> contours, final Pitch[] meanpitches) {
        contours.removeIf(c -> {
            double distance = 0.0f;
            int count = 0;
            for (int i=0; i<c.size();i++) {
                if (c.getPitch(i) != null && meanpitches[i] != null) {
                    distance += Math.abs(c.getPitch(i).distanceCents(meanpitches[i]));
                    count += 1;
                }
            }

            if (count > 0) distance /= count;
            return distance > 1200.0f;
        });
    }
}
