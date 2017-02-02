package org.vitrivr.cineast.core.util.fft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

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
public class ForwardFFT {

    /** Data obtained by forward FFT. */
    private Complex[] data;

    /** Magnitude spectrum of the FFT data. May be null if it has not been obtained yet. */
    private double[] magnitudeSpectrum;

    /** Power spectrum of the FFT data. May be null if it has not been obtained yet. */
    private double[] powerSpectrum;

    /** Bin labels for the FFT data. May be null if it has not been obtained yet. */
    private float[] labels;

    /** Max/peak power in the power-spectrum. */
    private Double maxPower = null;

    /** Min power in the power-spectrum. */
    private Double minPower = null;

    /** WindowFunction to apply before forward transformation. Defaults to IdentityWindows (= no window). */
    private WindowFunction windowFunction = new IdentityWindow();

    /** FastFourierTransformer instance used with this class. */
    private FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

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
    public void transform(double[] data, WindowFunction window) {
        this.windowFunction = window;
        this.transform(data);
    }

    /**
     * Performs a forward fourier transformation on the provided, real valued data. The method makes sure,
     * that the size of the array is a power of two (for which the FFT class has been optimized) and pads
     * the data with zeros if necessary.
     *
     * @param data Data to be transformed.
     */
    public void transform(double[] data) {
        int actual_length = data.length;
        int valid_length = this.nextPowerOf2(actual_length);
        double[] reshaped = new double[valid_length];
        for (int i = 0; i<reshaped.length; i++) {
            if (i < actual_length) {
                reshaped[i] = data[i] * this.windowFunction.value(i, valid_length);
            } else {
                reshaped[i] = 0;
            }
        }
        this.data = transformer.transform(reshaped, TransformType.FORWARD);

        /* Reset the calculated properties. */
        this.maxPower = null;
        this.minPower = null;
        this.powerSpectrum = null;
        this.magnitudeSpectrum = null;
        this.labels = null;
    }


    /**
     * Returns the magnitude spectrum of the transformed data. If that spectrum has not been
     * calculated yet it will be upon invocation of the method.
     *
     * @return Array containing the magnitude for each frequency bin.
     */
    public double[] getMagnitudeSpectrum() {
        if (this.magnitudeSpectrum == null) {
            int bins = this.data.length / 2;
            this.magnitudeSpectrum = new double[bins];
            for(int i = 0; i < bins; i++)
                this.magnitudeSpectrum[i] = Math.sqrt(this.data[i].getReal() * this.data[i].getReal() + this.data[i].getImaginary() * this.data[i].getImaginary()) / (this.data.length * this.windowFunction.normalization(this.data.length));

        }

        return this.magnitudeSpectrum;
    }

    /**
     * Returns the power spectrum of the transformed data. If that spectrum has not been
     * calculated yet it will be upon invocation of the method.
     *
     * @return Array containing the power for each frequency bin.
     */
    public double[] getPowerSpectrum() {
        if (this.powerSpectrum == null) {
            int bins = this.data.length / 2;
            this.powerSpectrum = new double[bins];
            for(int i = 0; i < bins; i++)
                this.powerSpectrum[i] = (this.data[i].getReal() * this.data[i].getReal() + this.data[i].getImaginary() * this.data[i].getImaginary()) / (this.data.length * this.windowFunction.normalization(this.data.length));

        }
        return this.powerSpectrum;
    }

    /**
     * Returns the frequencies for the bins returned by the getPowerSpectrum() and getMagnitudeSpectrum()
     * methods. In order to obtain these frequencies, the original sampingrate must be known.
     *
     * @param samplingrate Rate at which the original data was sampled.
     * @return Label / frequency for each frequency bin.
     */
    public float[] binFrequencies(float samplingrate) {
        if (this.labels == null) {
            float bin_width = samplingrate / this.data.length;
            int bins = data.length / 2;
            this.labels = new float[bins];
            for (int bin = 0; bin < this.labels.length; bin++) {
                this.labels[bin] = bin * bin_width;
            }
        }
        return this.labels;
    }

    /**
     * Returns the maximum (peak value) power in the power spectrum
     *
     * @return Maximum power in the power spectrum.
     */
    public final double getMaxPower() {
        if (this.maxPower == null) {
            this.calculatePowerRange();
        }
        return this.maxPower;
    }

    /**
     * Returns the minimum power in the power spectrum
     *
     * @return Minimum power in the power spectrum.
     */
    public final double getMinPower() {
        if (this.minPower == null) {
            this.calculatePowerRange();
        }
        return this.minPower;
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
     *
     */
    private void calculatePowerRange() {
        double[] spectrum = this.getPowerSpectrum();
        this.maxPower = 0.0;
        this.minPower = 0.0;
        for (int i=0;i<spectrum.length;i++) {
            this.maxPower = Math.max(spectrum[i], this.maxPower);
            this.minPower = Math.min(spectrum[i], this.minPower);
        }
    }

    /**
     * Helper method that can be used to find the closest power of two value greater than or equal to the provided value.
     * Used to introduce a zero-padding on input-data whose length is not equal to 2^n.
     *
     * @param number Value for which a power of two must be found.
     * @return Next value which is greater than or equal to the provided number and a power of two.
     */
    private int nextPowerOf2(int number) {
        return (int)Math.pow(2, Math.ceil(Math.log(number)/Math.log(2)));
    }
}
