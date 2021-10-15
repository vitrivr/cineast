package org.vitrivr.cineast.core.util.audio.pitch.estimation;

import org.vitrivr.cineast.core.util.audio.pitch.Pitch;
import org.vitrivr.cineast.core.util.dsp.fft.FFT;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.midi.MidiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class can be used to estimate the most salient pitch(es) from a provided FFT or STFT by applying the method
 * described in [1].
 *
 * The method sums the amplitudes of pitch-candidates and their harmonic partials in a frame to obtain a salience function.
 * The pitch candidate with the highest salience is then selected and removed from the spectrum. This procedure is repeated
 * until all pitches have been detected.
 *
 * [1] Klapuri, A. (2006). Multiple Fundamental Frequency Estimation by Summing Harmonic Amplitudes.
 *      Proceedings of the International Symposium/Conference on Music Information Retrieval (ISMIR), 216–221.
 *
 */
public class KLF0PitchEstimator {
    /** MIDI index of the minimum pitch to consider in the analysis. */
    private static final int DEFAULT_MIN_PITCH = 28;

    /** MIDI index of the maximum pitch to consider in the analysis. */
    private static final int DEFAULT_MAX_PITCH = 96;

    /** α value as defined in [1]. Suitable only for an analysis window of 96ms. */
    private static final float ALPHA = 27.0f;

    /** β value as defined in [1]. Suitable only for an analysis window of 96ms. */
    private static final float BETA = 320.0f;

    /** Maximum pitch to consider (MIDI index of the pitch). */
    private final int max;

    /** Maximum pitch to consider (MIDI index of the pitch). */
    private final int min;

    /** Maximum pitch to consider (MIDI index of the pitch). */
    private final float alpha;

    /** Maximum pitch to consider (MIDI index of the pitch). */
    private final float beta;

    /**
     * Default constructor for KLF0PitchEstimator. Uses parameter described in [1].
     */
    public KLF0PitchEstimator() {
        this(DEFAULT_MIN_PITCH, DEFAULT_MAX_PITCH, ALPHA, BETA);
    }

    /**
     *
     * @param min
     * @param max
     */
    public KLF0PitchEstimator(int min, int max, float alpha, float beta) {
        this.max = max;
        this.min = min;
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Estimates the pitches in the provided STFT and returns a List of PitchCandidate lists (one
     * list per FFT).
     *
     * @param stft STFT for which to estimate the pitches.
     * @return List of PitchCandidate lists.
     */
    public List<List<Pitch>> estimatePitch(STFT stft) {
        List<List<Pitch>> results = new ArrayList<>(stft.getStft().size());
        for (FFT fft : stft.getStft()) {
            if (fft.isZero()) {
              continue;
            }
            results.add(this.estimatePitch(fft));
        }
        return results;
    }


    /**
     * Estimates the pitches from the provided FFT and returns them as a list
     * of PitchCandidates.
     *
     * @param fft FFT to estimate the pitches from.
     * @return List of pitch candidates.
     */
    public List<Pitch> estimatePitch(FFT fft) {
        /* Prepare required helper variables. */
        final float samplingrate = fft.getSamplingrate();
        final int windowsize = fft.getWindowsize();
        final Spectrum spectrum = fft.getPowerSpectrum();

        /* Prepare empty array of booleans holding the estimates. */
        List<Pitch> candidates = new ArrayList<>();

        float test = 0, lasttest = 0;
        int loopcount = 1;
        while (true) {
            /* Detect new candidate. */
            Pitch candidate = this.detect(spectrum, samplingrate, windowsize);

            /* Test if that candidate already exists. */
            boolean exists = false;
            for (Pitch c : candidates) {
                if (c.getFrequency() == candidate.getFrequency()) {
                    c.setSalience(candidate.getSalience() + c.getSalience());
                    exists = true;
                    break;
                }
            }

            if (!exists) {
              candidates.add(candidate);
            }

            /* Conduct test and break if it fails. */
            lasttest = test;
            test = (float)((test + candidate.getSalience()) / Math.pow(loopcount, .7f));
            if (test <= lasttest) {
              break;
            }
            loopcount++;

            /* Add candidate to list. */
            candidates.add(candidate);

            final float f0 = candidate.getFrequency();
            final float tau = samplingrate/f0; /* Fundamental period, i.e. f0=fs/τ. */
            final float dtau = 0.25f; /* Δτ/2, which is 0.25 according to [1]. */

            /* Subtract the information of the found pitch from the current spectrum. */
            for (int m = 1; m * candidate.getFrequency() < samplingrate / 2; m++) {
                int max = Math.round((m*windowsize)/(tau - dtau));
                int min = Math.round((m*windowsize)/(tau + dtau));
                int max_bin = min;
                for (int offset = min; offset <= max && offset < windowsize/2; offset++) {
                    if (spectrum.getValue(offset) > spectrum.getValue(max_bin)) {
                        max_bin = offset;
                    }
                }
                spectrum.setValue(max_bin, spectrum.getValue(max_bin) - spectrum.getValue(max_bin) * this.g(f0, m));
            }
        }

        /* Sort list of candidates by their salience in descending order. */
        candidates.sort(Comparator.comparingDouble(Pitch::getSalience));
        Collections.reverse(candidates);

        /* Return list of candidates. */
        return candidates;
    }

    /**
     * Detects the most salient F0 candidate in the provided spectrum.
     *
     * @param spectrum Power spectrum to search for the candidate.
     * @param samplingrate Samplingrate at which the original signal has been sampled.
     * @param windowsize Windowsize used in the FFT.
     */
    private Pitch detect(Spectrum spectrum, final float samplingrate, final int windowsize) {
        Pitch candidate = null;
        for (int n = this.min; n<= this.max; n++) {
            final float f0 = MidiUtil.midiToFrequency(n);
            final double salience = this.salience(f0, spectrum, samplingrate, windowsize);
            if (candidate == null || candidate.getSalience() < salience) {
                candidate = new Pitch(f0);
                candidate.setSalience(salience);
            }
        }
        return candidate;
    }

    /**
     * Calculates and returns the salience of a f0 in a spectrum according to [1].
     *
     * @param f0 The f0 to calculate the salience for.
     * @param spectrum The spectrum to check.
     * @param samplingrate The rate at which the original audio has been sampled.
     * @param samplingrate The windowsize used during creation of the spectrum.
     * @return Salience of f0 in the spectrum.
     */
    private final double salience(float f0, Spectrum spectrum, final float samplingrate, final int windowsize) {
        final float tau = samplingrate/f0; /* Fundamental period, i.e. f0=fs/τ. */
        final float dtau = 0.25f; /* Δτ/2, which is 0.25 according to [1]. */
        float salience = 0; /* Salience of the candidate pitch. */

        for (int m = 1; m * f0 < samplingrate/2; m++) {
            int max = Math.round((m*windowsize)/(tau - dtau));
            int min = Math.round((m*windowsize)/(tau + dtau));
            int max_bin = min;
            for (int offset = min; offset <= max && offset < windowsize/2; offset++) {
                if (spectrum.getValue(offset) > spectrum.getValue(max_bin)) {
                    max_bin = offset;
                }
            }
            salience += spectrum.getValue(max_bin) * this.g(f0, m);
        }

        return salience;
    }


    /**
     * Returns the value of the weight-function for pitch-salience calculation
     * according to [1].
     *
     * @param f0 Fundamental frequency of the pitch candidate.
     * @param m Index of the partial (i.e. m-th partial -> fm = m*f0)
     * @return weight for the provided f0/partial constellation.
     */
    private double g(float f0, int m) {
        return (f0 + this.alpha)/(m*f0 + this.beta);
    }
}
