package org.vitrivr.cineast.core.util.fft;

/**
 * Some utility functions in the context of FFT and STFT.
 *
 * @author rgasser
 * @version 1.0
 * @created 04.02.17
 */
public class FFTUtil {

    private FFTUtil() { }

    /**
     * Returns frequency labels in Hz for a FFT of the specified size and samplerate.
     *
     * @param size Size of the FFT (i.e. number of frequency bins).
     * @param samplerate Samplerate at which the original data has been sampled.
     *
     * @return Array containing the frequency labels in Hz in ascending order.
     */
    public static float[] binFrequencies(int size, float samplerate) {
        double bin_width = (samplerate / size);
        double offset = bin_width/2.0;
        float[] labels = new float[size/2];
        for (int bin = 0; bin < labels.length; bin++) {
            labels[bin] = (float)((bin * bin_width) + offset);
        }
        return labels;
    }

    /**
     * Returns time labels in seconds for a STFT of given width using the provided
     * window size and samplerate.
     *
     * @param width Size of the STFT (i.e. number of time bins).
     * @param windowsize Used for FFT (i.e. number of samples per time bin).
     * @param samplerate Samplerate at which the original data has been sampled.
     *
     * @return Array containing the time labels for the STFT in seconds in ascending order.
     */
    public static float[] time(int width, int windowsize, float samplerate) {
        float[] labels = new float[width];
        for (int i=0;i<labels.length;i++) {
            labels[i] = i*(windowsize/samplerate);
        }
        return labels;
    }

    /**
     * Method that can be used to find the closest power of two value greater than or equal to the provided value.
     * Used to introduce a zero-padding on input-data whose length is not equal to 2^n.
     *
     * @param number Value for which a power of two must be found.
     * @return Next value which is greater than or equal to the provided number and a power of two.
     */
    public static int nextPowerOf2(int number) {
        return (int)Math.pow(2, Math.ceil(Math.log(number)/Math.log(2)));
    }

    /**
     * Checks if the provided number is a power of two and return true if so and false otherwise.
     *
     * @param number Number to check.
     * @return true if number is a power of two, false otherwise.
     */
    public static boolean isPowerOf2(int number) {
       double value = Math.log(number)/Math.log(2);
       return Math.ceil(value) == value;
    }
}
