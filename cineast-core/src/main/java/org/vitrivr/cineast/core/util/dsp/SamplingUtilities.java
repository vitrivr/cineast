package org.vitrivr.cineast.core.util.dsp;


public class SamplingUtilities {



    private SamplingUtilities() {}


    /**
     * Down-samples a 1D array by the provided downscaling factor and then returns the
     * down-sampled version of the original array.
     *
     * @param samples Double array that should be down-sampled.
     * @param factor Factor by which to down-sample. i.e. factor of n means that every n'th sample is discarded.
     * @return
     */
    public static double[] downsample (double[] samples, int factor) {
        /* Makes sure, that the factor is a positive value. */
        factor = Math.abs(factor);

        /* Make sure that array is large enough for down-sampling. */
        if (samples.length < factor) {
            throw new IllegalArgumentException(String.format("The provided array of samples (length: %d) is too small to be down-sampled by a factor of %d.", samples.length,  factor));
        }

        /* Now downsample. */
        double[] downsampled = new double[(samples.length/factor) - 1];
        int j = 0;
        for (int i=factor; i<(samples.length-factor); i+=factor, j+=1) {
            downsampled[j] = samples[i];
        }
        return downsampled;
    }
}
