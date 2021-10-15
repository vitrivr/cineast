package org.vitrivr.cineast.core.util.audio;

import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class calculates Mel-frequency cepstral coefficient (MFCC) features from the STFT of an audio signal.
 * The code was partially taken from the OrangeCow Volume project. See [1]
 *
 * [1] S. Pfeiffer, and C. Parker, and T. Vincent. 2005.
 *      OC Volume: Java speech recognition engine. 2005. [March 8th, 2017].
 *
 */
public class MFCC {

    /** Minimum frequency to consider. Defaults to 100Hz as per [1]. */
    private final float minFrequency;

    /** Number of Mel filters to use. Defaults to 23 as per [1]. */
    private final int melfilters;

    /** Array holding the cepstrum coefficients (cepstra). Defaults to 13 as per [1]. */
    private float[] cepstra;

    /**
     * Default constructor for MFCC class with 13 cepstra, a Mel-Filter bank of size
     * 13 and a minimum frequency of 133.0f Hz.
     */
    public MFCC() {
        this(13, 23, 133.0f);
    }

    /**
     * Constructor for MFCC.
     *
     * @param cepstra The number of cepstra to obtain for the MFCC feature.
     * @param melfilters The number of triangular mel-filters (size of the mel-filter bank).
     * @param minFrequency Minimum frequency to consider for MFCC feature.
     */
    public MFCC(int cepstra, int melfilters, float minFrequency) {
        this.melfilters = melfilters;
        this.minFrequency = minFrequency;
        this.cepstra = new float[cepstra];
    }

    /**
     * Returns a list of MFCC features for the provided STFT using the default settings.
     *
     * @param stft STFT to derive the MFCC features from.
     */
    public static List<MFCC> calculate(STFT stft) {
        List<Spectrum> spectra = stft.getMagnitudeSpectrum();
        List<MFCC> list = new ArrayList<>(spectra.size());
        for (Spectrum spectrum : spectra) {
            MFCC mfcc = new MFCC();
            mfcc.calculate(spectrum, stft.getSamplingrate(), stft.getNumberOfBins());
            list.add(mfcc);
        }
        return list;
    }

    /**
     * Returns a list of MFCC features for the provided STFT using the provided settings.
     *
     * @param stft STFT to derive the MFCC features from.
     * @param cepstra The number of cepstra to obtain for the MFCC feature.
     * @param melfilters The number of triangular mel-filters (size of the mel-filter bank).
     * @param minFrequency Minimum frequency to consider for MFCC feature.
     * @return
     */
    public static List<MFCC> calculate(STFT stft, int cepstra, int melfilters, float minFrequency) {
        List<Spectrum> spectra = stft.getMagnitudeSpectrum();
        List<MFCC> list = new ArrayList<>(spectra.size());
        for (Spectrum spectrum : spectra) {
            MFCC mfcc = new MFCC(cepstra, melfilters, minFrequency);
            mfcc.calculate(spectrum, stft.getSamplingrate(), stft.getNumberOfBins());
           list.add(mfcc);
        }
        return list;
    }

    /**
     * Finds and returns the center frequency of the i-th mel filter out of a specified amount
     * of filters.
     *
     * @param i The index of the filter for which a frequency should be obtained (zero based).
     * @param total The total amount of mel filters.
     * @param samplingrate The rate at which the original data has been sampled.
     * @param minFrequency The minimum frequency to consider.
     *
     * @return Center frequency (normal) of the i-th mel filter.
     */
    public static double centerFrequency(int i, int total, float samplingrate, float minFrequency) {
        double minMel = frequencyToMel(minFrequency);
        double maxMel = frequencyToMel(samplingrate/2);
        return melToFrequency(minMel + ((maxMel - minMel) / (total + 1)) * i);
    }

    /**
     * Transforms a frequency in Hz into a corresponding frequency on the mel scale and
     * returns the value of that mel frequency.
     *
     * @param frequency Frequency to transform.
     * @return Frequency on the mel scale corresponding to the normal frequency.
     */
    public static double frequencyToMel(double frequency) {
        return 2595 * Math.log10(1 + frequency/700.0);
    }

    /**
     * Transforms a mel frequency into a frequency in Hz and returns the value of that
     * frequency.
     *
     * @param mel Mel frequency to transform.
     * @return Value corresponding to the normal frequency.
     */
    public static double melToFrequency(double mel) {
        return 700.0 * Math.pow(10, mel/2595.0) - 700.0;
    }

    /**
     *
     * @param spectrum
     * @param samplingrate
     * @param windowsize
     */
    public void calculate(Spectrum spectrum, float samplingrate, int windowsize) {
        /* Check the type of the provided spectrum* */
        if (spectrum.getType() != Spectrum.Type.MAGNITUDE) {
          throw new IllegalArgumentException("A magnitude spectrum is required in order to calculate the Mel Frequency Cepstrum Coefficients.");
        }

        /* */
        int[] bin_indices = this.melFrequencyBins(samplingrate, windowsize);

        /* Calculates and returns the Mel Filter Bank values of the power-spectrum. */
        double[] mel_filter_banks = this.melFilter(spectrum.array(), bin_indices);

        /* Calculate and assign cepstra coefficients. */
        for (int i = 0; i < this.cepstra.length; i++){
            for (int j = 1; j <= this.melfilters; j++){
                this.cepstra[i] += mel_filter_banks[j - 1] * Math.cos(Math.PI * i / this.melfilters * (j - 0.5));
            }
        }
    }

    /**
     * Calculates and returns the indices of the frequency-bins relevant for mel-filtering (relative
     * to the full list of frequency bins).
     *
     * @param samplingrate The rate at which the original data has been sampled.
     * @param windowsize Windowsize used for FFT.
     * @return Array of bin-indices.
     */
    public int[] melFrequencyBins(float samplingrate, int windowsize) {
        int[] bins = new int[this.melfilters + 2];
        bins[0] = Math.round(this.minFrequency / samplingrate * windowsize);
        bins[bins.length -1] = (windowsize/2);
        for (int i = 1; i <= this.melfilters; i++){
            double f = centerFrequency(i, this.melfilters, samplingrate, this.minFrequency);
            bins[i] = (int)Math.round(f / samplingrate * windowsize);
        }
        return bins;
    }

    /**
     * Applies the triangular Mel-filters to the magnutude-spectrum
     *
     * @param magnitudes Linear magnitude spectrum to apply the mel-filters to.
     * @param indices Indices to use as center-frequencies.
     * @return Mel-scaled magnitude spectrum.
     */
    private double[] melFilter(double magnitudes[], int indices[]){
        double temp[] = new double[this.melfilters + 2];

        for (int k = 1; k <= this.melfilters; k++){
            double num1 = 0, num2 = 0;

            for (int i = indices[k - 1]; i <= indices[k]; i++){
                num1 += ((i - indices[k - 1] + 1) / (indices[k] - indices[k-1] + 1)) * magnitudes[i];
            }

            for (int i = indices[k] + 1; i <= indices[k + 1]; i++){
                num2 += (1 - ((i - indices[k]) / (indices[k + 1] - indices[k] + 1))) * magnitudes[i];
            }

            temp[k] = num1 + num2;
        }

        final double floor = -50.0;
        double fbank[] = new double[this.melfilters];
        for (int i = 0; i < this.melfilters; i++){
            fbank[i] = Math.max(Math.log(temp[i + 1]), floor);
        }

        return fbank;
    }

    /**
     * Getter for the cepstra.
     *
     * @return List of cepstra for the current MFCC.
     */
    public float[] getCepstra() {
        return cepstra;
    }
}
