package org.vitrivr.cineast.core.util.dsp.filter.spectrum;

import org.vitrivr.cineast.core.util.dsp.fft.FFT;
import org.vitrivr.cineast.core.util.dsp.fft.Spectrum;

/**
 * @author rgasser
 * @version 1.0
 * @created 16.04.17
 */
public interface SpectrumFilterInterface {
    /**
     * Returns a filtered Spectrum by applying a filter to the provided FFT.
     *
     * @param fft FFT to derive the spectrum from.
     * @return Filtered spectrum
     */
    Spectrum filteredSpectrum (FFT fft);
}
