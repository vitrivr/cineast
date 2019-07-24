package org.vitrivr.cineast.core.util.dsp.filter.frequency;

import org.apache.commons.math3.complex.Complex;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;

import java.util.Arrays;


/**
 * Implements a spectral whitening filter as described in [1].
 *
 * [1] Klapuri, A. (2006). Multiple Fundamental Frequency Estimation by Summing Harmonic Amplitudes.
 *      Proceedings of the International Symposium/Conference on Music Information Retrieval (ISMIR), 216–221.
 *
 * @author rgasser
 * @version 1.0
 * @created 16.04.17
 */
public class SpectralWhiteningFilter implements FrequencyDomainFilterInterface {
    /**
     * Represents a filter-band Hb with a triangular response.
     */
    public class FilterBand {
        /** Index of the lowest band-frequency (cut-off) in the provided FFT bin array. */
        private final int lowerBandIndex;

        /** Center-frequency of the band. */
        private final int centerBandIndex;

        /** Index of the highest band-frequency (cut-off) in the provided FFT bin array. */
        private final int upperBandIndex;

        /** Triangular weights in the filter band.*/
        private float[] weights;

        /**
         * Constructor for filter band.
         *
         * @param bandindex Index of the filter band.
         */
        private FilterBand(int bandindex) {
            this.lowerBandIndex = FFTUtil.binIndex(SpectralWhiteningFilter.this.centerfrequencies[bandindex-1], SpectralWhiteningFilter.this.windowsize,  SpectralWhiteningFilter.this.samplingrate);
            this.centerBandIndex = FFTUtil.binIndex(SpectralWhiteningFilter.this.centerfrequencies[bandindex], SpectralWhiteningFilter.this.windowsize,  SpectralWhiteningFilter.this.samplingrate);
            this.upperBandIndex = FFTUtil.binIndex(SpectralWhiteningFilter.this.centerfrequencies[bandindex+1], SpectralWhiteningFilter.this.windowsize,  SpectralWhiteningFilter.this.samplingrate);

            int length = upperBandIndex-lowerBandIndex+1;

            this.weights = new float[length];
            for (int i=0;i<weights.length;i++) {
                weights[i] = 1.0f-Math.abs((float)(centerBandIndex-(lowerBandIndex+i))/(float)(length));
            }
        }
    }


    /** Size of the FFT window. */
    private final int windowsize;

    /** Rate at which the original signal has been sampled. */
    private final float samplingrate;

    /** Amount of spectral whitening between 0.0 and 1.0 that is applied to a FFT. */
    private final float amount;

    /** Number of filter-bands to use. */
    private final int bands;

    /** Center-frequencies in the critical bands. */
    private float[] centerfrequencies;

    /** Array holding the FilterBands. */
    private FilterBand[] filterbands;

    /**
     * Constructor for the SpectralWhiteningFilter class.
     *
     * @param windowsize Size of the FFT window.
     * @param samplingrate Rate at which the original signal has been sampled.
     * @param amount Amount of spectral whitening between 0.0 and 1.0
     * @param bands Number of filter-bands to use.
     */
    public SpectralWhiteningFilter(int windowsize, float samplingrate, float amount, int bands) {
        this.windowsize = windowsize;
        this.samplingrate = samplingrate;
        this.amount = amount;
        this.bands = bands;

        this.centerfrequencies = new float[bands+2];
        this.filterbands = new FilterBand[bands];
        centerfrequencies[0] = 0.0f;
        for (int b = 1; b < this.centerfrequencies.length; b++) {
            this.centerfrequencies[b] = (float)(229.0 * (Math.pow(10, (b+1)/21.4) - 1));
        }
        for (int i = 0; i < this.filterbands.length; i++) {
            this.filterbands[i] = new FilterBand(i+1);
        }
    }

    /**
     * Applies the SpectralWhiteningFilter in place and thereby smoothens the spectral
     * envelope.
     *
     * @param fftbins Array of complex FFT bins.
     * @return Copied FFT bins after application of spectral whitening.
     */
    @Override
    public Complex[] filter(Complex[] fftbins) {
        Complex[] copy = Arrays.copyOf(fftbins, fftbins.length); /* Copies only the array. This is okay because Complex datatype is copied whenever altered. */
        return filterInPlace(copy);
    }

    /**
     * Applies the SpectralWhiteningFilter in place and thereby smoothens the spectral
     * envelope.
     *
     * <strong>Important: </strong> This method is applied in place and changes the values
     * in the original array.
     *
     * @param fftbins Array of complex FFT bins.
     * @return FFT bins after application of spectral whitening.
     */
    @Override
    public Complex[] filterInPlace(Complex[] fftbins) {
        /* Calculates the compression factor per band. */
        double[] compression = new double[filterbands.length];
        for (int i=0; i<filterbands.length; i++) {
            FilterBand band = this.filterbands[i];
            double sigma = 0.0;
            for (int j=band.lowerBandIndex; j<=band.upperBandIndex; j++) {
                sigma += band.weights[j-band.lowerBandIndex] * Math.pow(fftbins[j].abs(),2);
            }
            compression[i] = Math.pow(Math.sqrt(sigma / fftbins.length), this.amount-1);
        }

        /* Applies the linear interpolation of two compression factors between two center frequencies. */
        for (int i=0; i<filterbands.length-1; i++) {
            FilterBand band0 = this.filterbands[i];
            FilterBand band1 = this.filterbands[i+1];

            double c0 = compression[i];
            double c1 = compression[i+1];
            double scale = (c1-c0)/(band1.centerBandIndex - band0.centerBandIndex);

            for (int k=band0.centerBandIndex; k<band1.centerBandIndex; k++) {
                fftbins[k] = fftbins[k].multiply(c0 + (k-band0.centerBandIndex)*scale);
            }
        }
        return fftbins;
    }
}
