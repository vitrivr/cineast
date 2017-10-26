package org.vitrivr.cineast.core.util.dsp.fft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.vitrivr.cineast.core.util.dsp.fft.windows.RectangularWindow;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;
import org.vitrivr.cineast.core.util.dsp.filter.frequency.FrequencyDomainFilterInterface;

/**
 * This class wraps the Apache Commons FastFourierTransformer and extends it with some additional functionality.
 *
 * <ol>
 *     <li>It allows to apply WindowFunctions for forward-transformation. See WindowFunction interface!</li>
 *     <li>It provides access to some important derivatives of the FFT, like the power-spectrum. </li>
 *     <li>All derivatives are calculated in a lazy way i.e. the values are on access.</li>
 * </ol>
 *
 * The same instance of the FFT class can be re-used to process multiple samples. Every call to forward() will replace
 * all the existing data in the instance.
 *
 * The inspiration for this class comes from the FFT class found in the jAudio framework (see
 * https://github.com/dmcennis/jaudioGIT)
 *
 * @see WindowFunction
 *
 * @author rgasser
 * @version 1.0
 * @created 02.02.17
 */
public class FFT {
    /** Data obtained by forward FFT. */
    private Complex[] data;

    /** Magnitude spectrum of the FFT data. May be null if it has not been obtained yet. */
    private Spectrum magnitudeSpectrum;

    /** Power spectrum of the FFT data. May be null if it has not been obtained yet. */
    private Spectrum powerSpectrum;

    /** WindowFunction to apply before forward transformation. Defaults to IdentityWindows (= no window). */
    private WindowFunction windowFunction = new RectangularWindow();

    /** Samplingrate of the last chunk of data that was processed by FFT. */
    private float samplingrate;

    /**
     * Performs a forward fourier transformation on the provided, real valued data. The method makes sure,
     * that the size of the array is a power of two (for which the FFT class has been optimized) and pads
     * the data with zeros if necessary. Furthermore, one can provide a WindowingFunction that will be applied
     * on the data.
     *
     * <strong>Important: </strong>Every call to forward() replaces all the existing data in the current instance. I.e.
     * the same instance of FFT can be re-used.
     *
     * @param data Data to be transformed.
     * @param window WindowFunction to use for the transformation.
     */
    public void forward(double[] data, float samplingrate, WindowFunction window) {
        this.windowFunction = window;
        this.samplingrate = samplingrate;

        int actual_length = data.length;
        int valid_length = FFTUtil.nextPowerOf2(actual_length);
        double[] reshaped = new double[valid_length];
        for (int i = 0; i<reshaped.length; i++) {
            if (i < actual_length) {
                reshaped[i] = data[i] * this.windowFunction.value(i, valid_length);
            } else {
                reshaped[i] = 0;
            }
        }

        /* Perform FFT using FastFourierTransformer library. */
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        this.data = transformer.transform(reshaped, TransformType.FORWARD);

        /* Reset the calculated properties. */
        this.powerSpectrum = null;
        this.magnitudeSpectrum = null;
    }

    /**
     * Returns the magnitude spectrum of the transformed data. If that spectrum has not been
     * calculated yet it will be upon invocation of the method.
     *
     * @return Array containing the magnitude for each frequency bin.
     */
    public Spectrum getMagnitudeSpectrum() {
        if (this.magnitudeSpectrum == null) {
            this.magnitudeSpectrum = Spectrum.createMagnitudeSpectrum(this.data, this.samplingrate, this.windowFunction);
        }

        return this.magnitudeSpectrum;
    }

    /**
     * Returns the power spectrum of the transformed data. If that spectrum has not been
     * calculated yet it will be upon invocation of the method.
     *
     * @return Array containing the power for each frequency bin.
     */
    public Spectrum getPowerSpectrum() {
        if (this.powerSpectrum == null) {
            this.powerSpectrum = Spectrum.createPowerSpectrum(this.data, this.samplingrate, this.windowFunction);
        }
        return this.powerSpectrum;
    }

    /**
     * Getter for the transformed data.
     *
     * @return Array containing the raw FFT data.
     */
    public final Complex[] getValues() {
        return this.data;
    }

    /**
     * Can be used to directly access a FFT coefficient at the
     * specified index.
     *
     * @param index Index of the coefficient that should be retrieved.
     * @return Fourier coefficient.
     */
    public final Complex get(int index) {
        return this.data[index];
    }

    /**
     * Getter for samplingrate.
     *
     * @return Rate at which the original signal has been sampled.
     */
    public final float getSamplingrate() {
        return this.samplingrate;
    }

    /**
     * Getter for samplingrate.
     *
     * @return Rate at which the original signal has been sampled.
     */
    public final int getWindowsize() {
        return this.data.length;
    }

    /**
     * Returns true if the FFT only contains zeros and false
     * otherwise
     */
    public final boolean isZero() {
        for (Complex coefficient : this.data) {
            if (coefficient.abs() > 0) {
              return false;
            }
        }
        return true;
    }

    /**
     * Applies the provided FrequencyDomainFilter to this FFT.
     *
     * @param filter FrequencyDomainFilter that should be applied.
     */
    public final void applyFilter(FrequencyDomainFilterInterface filter) {
        filter.filterInPlace(this.data);
    }
}
