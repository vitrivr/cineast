package org.vitrivr.cineast.core.util.dsp.filter.time;

import java.util.Arrays;

/**
 * Interface for filters that are intended for application on raw sample data (i.e. time-domain).
 *
 * @author rgasser
 * @version 1.0
 * @created 08.03.17
 */
public interface TimeDomainFilterInterface {
    /**
     * Applies a filter onto an array of time-domain data (samples). This method is intended for
     * out-of-place operations i.e. a copy the original array is created before applying the filter.
     *
     * @param samples Array holding the samples.
     * @return Filtered samples.
     */
    default double[] filter(double[] samples) {
        double[] copy = Arrays.copyOf(samples, samples.length);
        return this.filterInPlace(copy);
    }

    /**
     * Applies a filter onto an array of time-domain data (samples). This method is intended for
     * in-place operations i.e. alters the original array.
     *
     * @param samples Array holding the samples.
     * @return Filtered samples.
     */
    double[] filterInPlace(double[] samples);
}
