package org.vitrivr.cineast.core.util.dsp.fft;

import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;
import org.vitrivr.cineast.core.util.dsp.filter.frequency.FrequencyDomainFilterInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 04.02.17
 */
public class STFT {
    /** Size of the FFT window. Must be a power of 2 (e.g. 512, 1024, 2048, 4096). */
    private final int windowsize;

    /** Sampling rate with which the samples have been sampled. */
    private final float samplingrate;

    /** Window function to use when calculating the FFT. */
    private final WindowFunction windowFunction;

    /**
     * Zero-padding factor used for STFT calculation.
     *
     * A value of X means that X zeros will be prepended before the signal start. Then after the actual signal, another X zeros
     * will be appended. This means, that the window will contain windowsize-2*X samplepoints
     */
    private final int padding;

    /** Overlap in samples between two subsequent windows. */
    private final int overlap;

    /** Height of the STFT (i.e. the number of frequency bins per FFT). */
    private int height;

    /** Frequency labels in ascending order (for all FFTs). */
    private final float[] frequencies;

    /** Time labels in ascending order (for each entry in the stft list). */
    private float[] time;

    /** List containing one FFT entry per timepoint. Same order as time[] */
    private final List<FFT> stft;

    /**
     *
     * @param windowsize
     * @param overlap
     * @param padding
     * @param function
     * @param samplingrate
     */
    public STFT(int windowsize, int overlap, int padding, WindowFunction function, float samplingrate) {
        /* Make sure that the windowsize is a power of two. */
        if (!FFTUtil.isPowerOf2(windowsize)) {
          throw new IllegalArgumentException("The provided window size of " + windowsize + " is not a power of two!");
        }

        /* Store the local variables. */
        this.samplingrate = samplingrate;

        /* Store the window size and window function. */
        this.windowFunction = function;
        this.windowsize = windowsize;
        this.overlap = overlap;
        this.padding = padding;
        this.height = windowsize/2;

        /* Prepares empty array for STFT. */
        this.stft = new ArrayList<>();

        this.frequencies = FFTUtil.binCenterFrequencies(windowsize, samplingrate);
    }

    /**
     * Performs a forward fourier transformation on the provided, real valued data and appends it to the FFT. The caller
     * of this function must make sure, that the data conforms to the properties specified upon construction of this class.
     * Otherwise, some results may be unexpected!
     *
     * <strong>Important: </strong>Every call to forward() appends a time-local DFT to the current STFT. The existing
     * data will be kept.
     *
     * @param samples
     */
    public void forward(double[] samples) {
        /* Initialize values for the sliding window. */
        final int increment = this.windowsize - overlap - 2*this.padding;
        final int length = this.windowsize - 2*this.padding;
        int start = 0;
        int end = start + length - 1;

        /* Initialize buffer that holds samples for FFT. */
        final double window[] = new double[windowsize];

        /*
         * Outer-loop: Create a sliding window and move it across the samples.
         * For each iteration, forward the FFT for the samples in the Window and add it to the list.
         */

        while (start < samples.length) {
            /* Copy the samples into the window. */
            if (end < samples.length) {
                System.arraycopy(samples, start, window, padding, length);
            } else {
                System.arraycopy(samples, start, window, padding, samples.length - start);
                Arrays.fill(window, samples.length - start + 1, window.length - 1, 0.0);
            }

            /* Create Forward FFT entries for each window. */
            FFT fft = new FFT();
            fft.forward(window, this.samplingrate, this.windowFunction);
            this.stft.add(fft);

            /* Move the window. */
            start += increment;
            end += increment;
        }

        /* Updates the time-labels. */
        this.time = FFTUtil.time(this.stft.size(), windowsize, overlap, padding, samplingrate);
    }

    /**
     * Assembles a list of power-spectra (one per FFT contained in this STFT)
     * and returns it.
     *
     * @return List of power-spectra
     */
    public List<Spectrum> getPowerSpectrum() {
        List<Spectrum> spectrum = new ArrayList<>(this.stft.size());
        for (FFT fft : this.stft) {
            spectrum.add(fft.getPowerSpectrum());
        }
        return spectrum;
    }

    /**
     * Assembles a list of magnitude-spectra (one per FFT contained in this STFT)
     * and returns it.
     *
     * @return List of magnitude-spectra
     */
    public List<Spectrum> getMagnitudeSpectrum() {
        List<Spectrum> spectrum = new ArrayList<>(this.stft.size());
        for (FFT fft : this.stft) {
            spectrum.add(fft.getMagnitudeSpectrum());
        }
        return spectrum;
    }

    /**
     * Getter for frequency bin labels.
     *
     * @return
     */
    public final float[] getFrequencies() {
        return frequencies;
    }

    /**
     * Returns the number of frequency-bins.
     */
    public final int getNumberOfBins() {
        return this.frequencies.length;
    }

    /**
     * Returns the size / width of an individual frequency bin.
     *
     * @return
     */
    public final float getBinSize() {
        return (this.samplingrate/this.windowsize);
    }

    /**
     * Getter for time labels.
     *
     * @return
     */
    public final float[] getTime() {
        return time;
    }

    /**
     * Getter for STFT.
     *
     * @return
     */
    public final List<FFT> getStft() {
        return Collections.unmodifiableList(this.stft);
    }

    /**
     * Getter for window-size.
     *
     * @return
     */
    public final int getWindowsize() {
        return this.windowsize;
    }

    /**
     * Getter for overlap value.
     *
     * @return
     */
    public int getOverlap() {
        return this.overlap;
    }

    /**
     * Getter for sampling rate.
     *
     * @return
     */
    public final float getSamplingrate() {
        return this.samplingrate;
    }

    /**
     * Getter for the WindowFunction.
     *
     * @return WindowFunction object.
     */
    public WindowFunction getWindowFunction() {
        return windowFunction;
    }

    /**
     * Getter for width of the STFT (i.e. number of timepoints).
     *
     * @return Width of the STFT.
     */
    public int getWidth() {
        return this.stft.size();
    }

    /**
     * Getter for height of the STFT (i.e. number of frequency bins).
     *
     * @return Height of the STFT.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * The stepsize in seconds between to adjacent bins in the time dimension.
     *
     * @return
     */
    public float timeStepsize() {
       return ((windowsize - overlap - 2*padding)/this.samplingrate);
    }

    /**
     * Applies the provided FrequencyDomainFilter to this FFT.
     *
     * @param filter FrequencyDomainFilter that should be applied.
     */
    public final void applyFilter(FrequencyDomainFilterInterface filter) {
        for (FFT fft : this.getStft()) {
            fft.applyFilter(filter);
        }
    }

}
