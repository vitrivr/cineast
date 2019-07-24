package org.vitrivr.cineast.core.util.audio;

import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;
import org.vitrivr.cineast.core.util.dsp.midi.MidiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to calculate the Harmonic Pitch Class Profile (HPCP) of a Short-Term Fourier Transform or a single
 * Spectrum using the approach outlined by E. Gomez in [1].
 *
 * Harmonic Pitch Class Profiles assign the frequencies in a power spectrum to the pitch classes on the equal tempered
 * scale. Depending on whether only semitones, 1/2 semitones or 1/3 semitones are considered, the HPCP feature vector
 * holds either 12, 24 or 36 entries.
 *
 * [1] Gómez, E. (2006). Tonal description of polyphonic audio for music content processing.
 *     INFORMS Journal on Computing, 18(3), 294–304. http://doi.org/10.1287/ijoc.1040.0126
 *
 * [2] Kurth F. & Müller M. (2008). Efficient Index-Based Audio Matching.
 *     IEEE TRANSACTIONS ON AUDIO, SPEECH, AND LANGUAGE PROCESSING
 *
 * @see STFT
 * @see Spectrum
 *
 * @author rgasser
 * @version 1.0
 * @created 03.02.17
 */
public class HPCP {
    /**
     * Defines the resolution of the HPCP (full semitones, 1/2 semitones or 1/3 semitones).
     */
    public enum Resolution {
        FULLSEMITONE (12),
        HALFSEMITONE (24),
        THIRDSEMITON (36);

        public final int bins;

        Resolution(int bins) {
            this.bins = bins;
        }
    }

    /** Window-size parameter, which defaults to 4/3 semitones as per [1]. */
    private static final float WINDOW = 4f/3f;

    /** Float array holding the HPCP data. */
    private final List<float[]> hpcp = new ArrayList<>();

    /** Minimum frequency to consider. Defaults to 100Hz as per [1]. */
    private final float minFrequency;

    /** Maximum frequency to consider. Defaults to 5000Hz as per [1]. */
    private final float maxFrequency;

    /** Resolution of the HPCP. */
    private final Resolution resolution;

    /**
     * Calculates the center-frequency of a bin defined by its bin number. The bin numbers
     * are defined such that bin 0 corresponds to F_REF (440 Hz). An increase of the bin number
     * is equivalent to moving 1, 1/2 or 1/3 of a semitone up on the scale.
     *
     * @param n Desired bin number
     * @return r Resolution to use (1, 1/2, or 1/3)
     */
    public static double binToCenterFrequency(int n, Resolution r) {
        return MidiUtil.F_REF*Math.pow(2, ((float)n/(float)r.bins));
    }

    /**
     * Default constructor. Creates an HPCP container for a full semitone, 12 entries HPCP that consider
     * frequencies between 100Hz and 5000Hz.
     */
    public HPCP() {
        this(Resolution.FULLSEMITONE, 100.0f, 5000.0f);
    }

    /**
     * Constructor for a custom HPCP container.
     *
     * @param resolution Desired resolution in semitones (1, 1/2 or 1/3)
     * @param minFrequency Minimum frequency to consider. Frequencies below that limit will be ignored.
     * @param maxFrequency Maximum frequency to consider. Frequencies above that limit will be ignored.
     */
    public HPCP(Resolution resolution, float minFrequency, float maxFrequency) {
        this.resolution = resolution;
        this.maxFrequency = maxFrequency;
        this.minFrequency = minFrequency;
    }

    /**
     * Adds the contribution of a STFT to the HPCP by adding the contributions all the power spectra
     * of that STFT to the HPCP.
     *
     * @param stft STFT that should contribute to the Harmonic Pitch Class Profile.
     */
    public void addContribution(STFT stft) {
        for (Spectrum spectrum : stft.getPowerSpectrum()) {
            this.addContribution(spectrum);
        }
    }

    /**
     * Adds the contribution of a single spectrum to the HPCP. The spectrum can either be a power
     * or magnitude spectrum.
     *
     * @param spectrum Spectrum that should contribute to the Harmonic Pitch Class Profile.
     */
    public void addContribution(Spectrum spectrum) {
        /* Prune the PowerSpectrum and the Frequencies to the range that is interesting according to min frequency and max frequency.*/
        Spectrum pruned = spectrum.reduced(this.minFrequency, this.maxFrequency);

        double threshold = 1.0e-8;
        List<Pair<Float,Double>> peaks = pruned.findLocalMaxima(threshold, true);
        float[] hpcp = new float[this.resolution.bins];

        /* For each of the semi-tones (according to resolution), add the contribution of every peak. */
        for (int n=0;n<this.resolution.bins;n++) {
            for (Pair<Float,Double> peak : peaks) {
                if (pruned.getType() == Spectrum.Type.POWER) {
                    hpcp[n] += peak.second * this.weight(n, peak.first);
                } else if (pruned.getType() == Spectrum.Type.MAGNITUDE) {
                    hpcp[n] += Math.pow(peak.second,2) * this.weight(n, peak.first);
                }
            }
        }

        /* */
        this.hpcp.add(hpcp);
    }

    /**
     * Returns the raw, un-normalized HPCP float array.
     *
     * @param idx Zero based index of the time-frame for which to return the HPCP.
     * @return Float array containing the HPCP.
     */
    public float[] getHpcp(int idx) {
        return hpcp.get(idx);
    }

    /**
     * Returns the HPCP vectorwhich has been normalized by the sum of its components as
     * proposed in [2].
     *
     * @param idx Zero based index of the time-frame for which to return the HPCP.
     * @return float array containing the sum-normalized HPCP for the given index.
     */
    public float[] getSumNormalizedHpcp(int idx) {
        float[] normHpcp = hpcp.get(idx);
        float sum = 0.0f;
        for (float aNormHpcp : normHpcp) {
          sum += aNormHpcp;
        }
        for (int i = 0; i< normHpcp.length; i++) {
          normHpcp[i] /= sum;
        }
        return normHpcp;
    }

    /**
     * Returns the HPCP vector which has been normalized by the value of its maximum component
     * as proposed in the original paper ([1]).
     *
     * @param idx Zero based index of the time-frame for which to return the HPCP.
     * @return float array containing the max-normalized HPCP for the given index.
     */
    public float[] getMaxNormalizedHpcp(int idx) {
        float[] normHpcp = hpcp.get(idx);
        float max = 0.0f;
        for (float aNormHpcp : normHpcp) {
          max = Math.max(max,aNormHpcp);
        }
        for (int i = 0; i< normHpcp.length; i++) {
          normHpcp[i] /= max;
        }
        return normHpcp;
    }

    /**
     * Returns the mean HPCP array (i.e. the HPCP arithmetic mean of all
     * HPCP contributions).
     *
     * @return Float array containing the mean HPCP.
     */
    public float[] getMeanHpcp() {
        float[] normalized = new float[this.resolution.bins];
        for (float[] hpcp : this.hpcp) {
            for (int i=0; i< this.resolution.bins;i++) {
                normalized[i] += hpcp[i]/(this.hpcp.size());
            }
        }
        return normalized;
    }

    /**
     * Returns the size of the HPCP which relates to the number of timepoints.
     *
     * @return Size of the HPCP.
     */
    public int size() {
        return this.hpcp.size();
    }

    /**
     * Getter for the HPCP resolution which gives an indication about the number of bins per timepoint.
     *
     * @return Resolution of the HPCP.
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Calculates the contribution of a given frequency (usually from a spectrum) to
     * a given bin in the HPCP.
     *
     * @param n Number of the bin.
     * @param frequency Frequency to calculate the contribution for.
     * @return Contribution of the frequency to the bin.
     */
    private double weight(int n, double frequency) {
        double frequency_n = binToCenterFrequency(n, this.resolution);


        double p = Math.log(frequency/frequency_n)/Math.log(2);

        int m1 = (int) Math.ceil(p) * (-1);
        int m2 = (int) Math.floor(p) * (-1);


        double distance = this.resolution.bins*Math.min(Math.abs(p + m1), Math.abs(p + m2));


        if (distance > 0.5 * WINDOW) {
            return 0;
        } else {
            return Math.pow(Math.cos((Math.PI/2) * (distance/(0.5 * WINDOW))),2);
        }
    }
}
