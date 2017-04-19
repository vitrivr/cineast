package org.vitrivr.cineast.core.util.audio;

import org.vitrivr.cineast.core.util.dsp.fft.FFT;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
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
 * @author rgasser
 * @version 1.0
 * @created 17.04.17
 */
public class KLF0PitchEstimator {
    /** MIDI index of the minimum pitch to consider in the analysis. */
    private static final int MIN_PITCH = 28;

    /** MIDI index of the maximum pitch to consider in the analysis. */
    private static final int MAX_PITCH = 96;

    /** α value as defined in [1]. Suitable only for an analysis window of 96ms. */
    private static final double ALPHA = 50.0f;

    /** β value as defined in [1]. Suitable only for an analysis window of 96ms. */
    private static final double BETA = 320.0f;

    /**
     * Represents a pitch / F0 candidate as returned by the KlapuriF0PitchEstimator.
     */
    public static class PitchCandidate {
        /* Frequency value of the pitch candidate. */
        public float frequency;

        /** Salience value of the pitch candidate. */
        public float salience;

        /** Boolean indicating if the candidate is active or not. */
        private boolean active = true;

        /**
         *
         * @param frequency
         * @param salience
         */
        private PitchCandidate(float frequency, float salience) {
            this.frequency = frequency;
            this.salience = salience;
        }

        /**
         *
         * @return
         */
        public boolean isActive() {
            return active;
        }

        /**
         *
         * @param active
         */
        public void setActive(boolean active) {
            this.active = active;
        }

    }

    /**
     * Estimates the pithces in the provided STFT and returns a List of PitchCandidate lists (one
     * list per FFT).
     *
     * @param stft STFT for which to estimate the pitches.
     * @return List of PitchCandidate lists.
     */
    public List<List<PitchCandidate>> estimatePitch(STFT stft) {
        List<List<PitchCandidate>> results = new ArrayList<>(stft.getStft().size());
        for (FFT fft : stft.getStft()) {
            if (fft.isZero()) continue;
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
    public List<PitchCandidate> estimatePitch(FFT fft) {
        /* Prepare required helper variables. */
        final float samplingrate = fft.getSamplingrate();
        final int windowsize = fft.getWindowsize();
        final Spectrum spectrum = fft.getPowerSpectrum();

        /* Prepare empty array of booleans holding the estimates. */
        List<PitchCandidate> candidates = new ArrayList<>();

        float test = 0, lasttest = 0;
        int loopcount = 1;
        while (true) {
            PitchCandidate candidate = this.detect(spectrum, samplingrate, windowsize);
            boolean exists = false;
            for (PitchCandidate c : candidates) {
                if (c.frequency == candidate.frequency) {
                    c.salience = candidate.salience;
                    exists = true;
                };
            }

            if (!exists) candidates.add(candidate);

            lasttest = test;
            test = (float)((test + candidate.salience) / Math.pow(loopcount, .7f));
            if (test <= lasttest) break;
            loopcount++;

            /* Subtract the information of the found pitch from the current spectrum. */
            for (int i = 1; i * candidate.frequency < samplingrate / 2; ++i) {
                int index = FFTUtil.binIndex(i * candidate.frequency, windowsize, samplingrate);
                float weighting = (candidate.frequency + 52) / (i * candidate.frequency + 320);
                spectrum.setValue(index, spectrum.getValue(index) *  (1 - 0.89f * weighting));
                spectrum.setValue(index-1, spectrum.getValue(index-1) *  (1 - 0.89f * weighting));
            }
        }

        /* Sort list of candidates by their salience in descending order. */
        candidates.sort(Comparator.comparingDouble(c -> c.salience));
        Collections.reverse(candidates);

        /* Return list of candidates. */
        return candidates;
    }

    /**
     * Detects the most salient F0 candidate in the provided spectrum.
     *
     * @param spectrum Power spectrum to search for the candidate. According to [1], spectral whitening should be
     *                 applied to the signal prior to F0 estimation.
     * @param samplingrate Samplingrate at which the original signal has been sampled.
     * @param windowsize Windowsize used in the FFT.
     */
    private PitchCandidate detect(Spectrum spectrum, final float samplingrate, final int windowsize) {
        PitchCandidate candidate = null;
        for (int n = MIN_PITCH; n<= MAX_PITCH; n++) {
            final float pitch = MidiUtil.midiToFrequency(n);
            final float tau = samplingrate/pitch; /* Fundamental period, i.e. f0=fs/τ. */
            final float dtau = 0.25f; /* Δτ/2, which is 0.25 according to [1]. */
            float cSalience = 0; /* Salience of the candidate pitch. */
            for (int m = 1; m * pitch < samplingrate / 2; m++) {
                int bin = FFTUtil.binIndex(m * pitch, windowsize, samplingrate);
                int max = Math.round((m*windowsize)/(tau - dtau));
                int min = Math.round((m*windowsize)/(tau + dtau));
                double val = 0;
                for (int offset = min; offset <= max; offset++) {
                    if (bin + offset < windowsize / 2) {
                        val = Math.max(val, spectrum.getValue(offset));
                    }
                }
                cSalience += val * this.g(pitch, m);
            }
            if (candidate == null || candidate.salience < cSalience) {
                candidate = new PitchCandidate(pitch, cSalience);
            }
        }
        return candidate;
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
        return (f0 + ALPHA)/(m*f0 + BETA);
    }
}
