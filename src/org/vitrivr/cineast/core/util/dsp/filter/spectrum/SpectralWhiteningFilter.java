package org.vitrivr.cineast.core.util.dsp.filter.spectrum;

import org.vitrivr.cineast.core.util.dsp.fft.FFT;
import org.vitrivr.cineast.core.util.dsp.fft.FFTUtil;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;

/**
 * Implements a spectral whitening filter as described in [1].
 *
 * [1] Klapuri, A. P. (2003). Multiple Fundamental Frequency Estimation Based on Harmonicity and Spectral Smoothness.
 *      IEEE Transactions on Speech and Audio Processing, 11(6), 804–816. http://doi.org/10.1109/TSA.2003.815516
 *
 * @author rgasser
 * @version 1.0
 * @created 16.04.17
 */
public class SpectralWhiteningFilter implements SpectrumFilterInterface{

    /** The maximum frequency to consider for spectral whitening. Frequencies above that threshold will be left untouched. */
    private final float maxFrequency;

    /** The minimum frequency to consider for spectral whitening. Frequencies bellow that threshold will be left untouched. */
    private final float minFrequency;

    /**
     * Constructor for SpectralWhiteningFilter
     *
     * @param minFrequency The minimum frequency to consider for spectral whitening.
     * @param maxFrequency The maximum frequency to consider for spectral whitening.
     */
    public  SpectralWhiteningFilter(float minFrequency, float maxFrequency) {
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;

    }


    /** Returns a filtered power spectrum by first warping the magnitude of the original power spectrum
     * and then subtracting the moving average.
     *
     * @param fft FFT to derive the spectrum from.
     * @return Spectral whitened power spectrum
     */
    @Override
    public Spectrum filteredSpectrum(FFT fft) {
        final int size =  fft.getWindowsize()/2;
        final int min_index = FFTUtil.binIndex(this.minFrequency, fft.getWindowsize(), fft.getSamplingrate());
        final int max_index = FFTUtil.binIndex(this.maxFrequency, fft.getWindowsize(), fft.getSamplingrate());
        final double g = this.scalingFactor(fft, min_index, max_index);

        /* Prepare array holding the whitened spectrum. */
        double[] whitened = new double[size];

        /* Performs magnitude wrapping. */
        for (int n=0;n<size;n++) {
            whitened[n] = Math.log1p((2*fft.get(n).abs())/g);
        }

        /* Subtracts the noise. */
        int start = min_index;
        while (start <= max_index) {
            /* Calculate the end of the band (2/3 Octave but a 100Hz minimum) */
            int end = (int)Math.pow(start, 4.0 / 3.0);
            if (end < (start + 5)) {
                end = start + 5;
            }
            end = Math.min(end, size-1);

            double average = 0;
            for (int n = size; n < end; n++) {
                average += whitened[n];
            }
            average /= ((max_index + 1)-min_index);
            for (int n = start; n<end; n++) {
                whitened[n] = Math.max(0, whitened[n]  - average);
            }
            start = end;
        }

        /* Creates and returns a new Spectrum .*/
        return new Spectrum(Spectrum.Type.POWER, whitened, FFTUtil.binCenterFrequencies(fft.getWindowsize(), fft.getSamplingrate()));
    }

    /**
     * Returns the scale factor for magnitude wrapping.
     *
     * @param fft FFF to derive the scale-factor for.
     * @param min_index Minimal frequency index.
     * @param max_index Maximum frequency index.
     * @return Scale factor
     */
    private double scalingFactor(FFT fft, int min_index, int max_index) {
        double g = 0;
        for (int i=min_index; i<=max_index; i++) {
            g += Math.pow(2*fft.get(i).abs(), 1.0/3.0);
        }
        return Math.pow(1.0/(max_index-min_index+1) * g, 3);
    }
}
