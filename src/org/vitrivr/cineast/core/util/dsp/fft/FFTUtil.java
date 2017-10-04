package org.vitrivr.cineast.core.util.dsp.fft;

import org.vitrivr.cineast.core.data.Pair;

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
     * Returns frequency labels in Hz for a FFT of the specified size and samplingrate.
     *
     * @param size Size of the FFT (i.e. number of frequency bins).
     * @param samplingrate Rate at which the original data has been sampled.
     *
     * @return Array containing the frequency labels in Hz in ascending order.
     */
    public static float[] binCenterFrequencies(int size, float samplingrate) {
        float[] labels = new float[size/2];
        for (int bin = 0; bin < labels.length; bin++) {
            labels[bin] = FFTUtil.binCenterFrequency(bin, size, samplingrate);
        }
        return labels;
    }

    /**
     * Returns the center frequency associated with the provided bin-index for the given
     * window-size and samplingrate.
     *
     * @param index Index of the bin in question.
     * @param size Size of the FFT (i.e. number of frequency bins).
     * @param samplingrate Rate at which the original data has been sampled.
     */
    public static float binCenterFrequency(int index, int size, float samplingrate) {
        if (index > size) {
          throw new IllegalArgumentException("The index cannot be greater than the window-size of the FFT.");
        }
        double bin_width = (samplingrate / size);
        double offset = bin_width/2.0;
        return (float)((index * bin_width) + offset);
    }

    /**
     * Returns the bin-index associated with the provided frequency at the given samplingrate
     * and window-size.
     *
     * @param frequency
     * @param size Size of the FFT (i.e. number of frequency bins).
     * @param samplingrate Rate at which the original data has been sampled.
     * @return
     */
    public static int binIndex(float frequency, int size, float samplingrate) {
        if (frequency > samplingrate/2) {
          throw new IllegalArgumentException("The frequency cannot be greater than half the samplingrate.");
        }
        double bin_width = (samplingrate / size);
        return (int)Math.floor(frequency/bin_width);
    }


    /**
     * Returns time labels in seconds for a STFT of given width using the provided
     * window size and samplerate.
     *
     * @param width Size of the STFT (i.e. number of time bins).
     * @param windowsize Used for FFT (i.e. number of samples per time bin)
     * @param overlap Overlap in samples between two adjacent windows during the FFT.
     * @param padding Zeropadding, i.e. how many zeros have been added before and after the actual sample starts
     *                (Assumption: padding happens within the fixed windowsize)
     * @param samplingrate Rate at which the original data has been sampled.
     *
     * @return Array containing the time labels for the STFT in seconds in ascending order.
     */
    public static float[] time(int width, int windowsize, int overlap, int padding, float samplingrate) {
        float[] labels = new float[width];
        float stepsize = FFTUtil.timeStepsize(windowsize, overlap, padding, samplingrate);
        for (int i=0;i<labels.length;i++) {
            labels[i] = i*stepsize;
        }
        return labels;
    }


    /**
     * Returns the width in seconds of a single FFT in an STFT i.e. how many seconds one
     * progresses in the original signal when moving to the next FFT.
     *
     * @param windowsize Windowsize used for the FFT.
     * @param overlap Overlap in samples between two adjacent windows during the FFT.
     * @param padding Zeropadding, i.e. how many zeros have been added before and after the actual sample starts
     *                (Assumption: padding happens within the fixed windowsize)
     * @param samplingrate Rate at which the original signal has been sampled.
     * @return Time step-size in seconds.
     */
    public static float timeStepsize(int windowsize, int overlap, int padding, float samplingrate) {
       return ((windowsize - overlap - 2*padding)/samplingrate);
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
     * Calculates and returns the windowsize in samples and the zero-padding in samples so as to achieve
     * a certain window-duration in seconds.
     *
     * @param samplingrate The samplingrate of the original data.
     * @param windowduration Duration of the window in seconds.
     * @return Pair of integers; first integer determines the windowsize and the second determines the padding.
     */
    public static Pair<Integer,Integer> parametersForDuration(float samplingrate, float windowduration){
        int samples = (int)(samplingrate * windowduration);
        int windowsize = nextPowerOf2(samples);
        return new Pair<>(windowsize,(windowsize-samples)/2);
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
