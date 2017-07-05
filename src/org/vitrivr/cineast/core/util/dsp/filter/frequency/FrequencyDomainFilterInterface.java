package org.vitrivr.cineast.core.util.dsp.filter.frequency;

import org.apache.commons.math3.complex.Complex;

/**
 * Interface for filters that are intended for application on data returned by a FFT (i.e. frequency-domain
 * data). The interface works with complex representations of the Fourier coefficients.
 *
 * @author rgasser
 * @version 1.0
 * @created 12.04.17
 */
public interface FrequencyDomainFilterInterface {

    /**
     * Applies a frequency-domain filter onto the provided FFT bins (raw FFT data). This
     * method is intended for out-of-place operations i.e. copies the original array and the
     * objects contained within before applying the filter.
     *
     * @param fftbins FFT bins onto which the filter is applied.
     * @return Filtered FFT bins.
     */
    Complex[] filter(Complex[] fftbins);

    /**
     * Applies a frequency-domain filter onto the provided FFT bins (raw FFT data). This
     * method is intended for in-place operations i.e. alters the original array.
     *
     * @param fftbins FFT bins onto which the filter is applied.
     * @return Filtered FFT bins.
     */
    Complex[] filterInPlace(Complex[] fftbins);

}
