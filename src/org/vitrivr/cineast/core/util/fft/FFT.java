package org.vitrivr.cineast.core.util.fft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.vitrivr.cineast.core.util.fft.windows.IdentityWindow;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

/**
 * This class wraps the Apache Commons FastFourierTransformer and extends it with some additional functionality.
 *
 * <ol>
 *     <li>It allows to apply WindowFunctions for forward-transformation. See WindowFunction interface!</li>
 *     <li>It provides access to some important derivatives of the FFT, like the power-spectrum. </li>
 *     <li>All derivatives are calculated  in a lazy way i.e. the values are on access.</li>
 * </ol>
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
    private WindowFunction windowFunction = new IdentityWindow();

    /**
     * Performs a forward fourier transformation on the provided, real valued data. The method makes sure,
     * that the size of the array is a power of two (for which the FFT class has been optimized) and pads
     * the data with zeros if necessary.
     *
     * Furthermore, one can provide a WindowingFunction that will be applied on the data.
     *
     * @param data Data to be transformed.
     * @param window WindowFunction to use for the transformation.
     */
    public void forward(double[] data, WindowFunction window) {
        this.windowFunction = window;
        this.forward(data);
    }

    /**
     * Performs a forward fourier transformation on the provided, real valued data. The method makes sure,
     * that the size of the array is a power of two (for which the FFT class has been optimized) and pads
     * the data with zeros if necessary.
     *
     * @param data Data to be transformed.
     */
    public void forward(double[] data) {
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
     * @param samplingrate Rate at which the original data has been sampled.
     *
     * @return Array containing the magnitude for each frequency bin.
     */
    public Spectrum getMagnitudeSpectrum(int samplingrate) {
        if (this.magnitudeSpectrum == null) {
            this.magnitudeSpectrum = Spectrum.createMagnitudeSpectrum(this.data, samplingrate, this.windowFunction);
        }

        return this.magnitudeSpectrum;
    }

    /**
     * Returns the power spectrum of the transformed data. If that spectrum has not been
     * calculated yet it will be upon invocation of the method.
     *
     * @param samplingrate Rate at which the original data has been sampled.
     *
     * @return Array containing the power for each frequency bin.
     */
    public Spectrum getPowerSpectrum(int samplingrate) {
        if (this.powerSpectrum == null) {
            this.powerSpectrum = Spectrum.createPowerSpectrum(this.data, samplingrate, this.windowFunction);
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
}
