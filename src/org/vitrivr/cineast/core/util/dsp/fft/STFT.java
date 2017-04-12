package org.vitrivr.cineast.core.util.dsp.fft;

import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;

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
    /** Sampling rate with which the samples have been sampled. */
    private final float samplingrate;

    /** Window function to use when calculating the FFT. */
    private final WindowFunction windowFunction;

    /** Size of the FFT window. Must be a power of 2 (e.g. 512, 1024, 2048, 4096). */
    private final int windowsize;

    /** Overlap in samples between two subsequent windows. */
    private final int overlap;

    /** Width of the STFT (i.e. the number of timepoints or FFT's). */
    private int width;

    /** Height of the STFT (i.e. the number of frequency bins per FFT). */
    private int height;

    /** Frequency labels in ascending order (for all FFTs). */
    private final float[] frequencies;

    /** Time labels in ascending order (for each entry in the stft list). */
    private final float[] time;

    /** List containing one FFT entry per timepoint. Same order as time[] */
    private final List<FFT> stft;

    /**
     *
     * @param windowsize
     * @param overlap
     * @param function
     * @param samplingrate
     */
    public STFT(int windowsize, int overlap, WindowFunction function, float samplingrate) {
        /* Make sure that the windowsize is a power of two. */
        if (!FFTUtil.isPowerOf2(windowsize)) throw new IllegalArgumentException("The provided window size of " + windowsize + " is not a power of two!");

        /* Store the local variables. */
        this.samplingrate = samplingrate;

        /* Store the window size and window function. */
        this.windowFunction = function;
        this.windowsize = windowsize;
        this.overlap = overlap;
        this.height = windowsize/2;

        /* Prepares empty array for STFT. */
        this.stft = new ArrayList<>();

        this.frequencies = FFTUtil.binFrequencies(windowsize, samplingrate);
        this.time = FFTUtil.time(this.width, windowsize, samplingrate);
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
        /* Derive properties from the available information. */
        this.width += (int) Math.ceil((float) samples.length / (windowsize - overlap));

        /* Outer-loop: Create a sliding window and move it across the samples.
         *
         * For each iteration, forward the FFT for the samples in the Window and add it to the list.
         */
        double window[] = new double[windowsize];
        for (int i = 0; i < this.width; i++) {
            int start = i * (windowsize - overlap);
            int end = start + windowsize;

            /* Copy the samples into the window. */
            if (end <= samples.length) {
                System.arraycopy(samples, start, window, 0, windowsize);
            } else {
                System.arraycopy(samples, start, window, 0, samples.length - start);
                Arrays.fill(window, samples.length - start + 1, window.length - 1, 0.0);
            }

            /* Create Forward FFT entries for each window. */
            FFT fft = new FFT();
            fft.forward(window, this.samplingrate, this.windowFunction);
            this.stft.add(fft);
        }
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
     *
     * @return
     */
    public WindowFunction getWindowFunction() {
        return windowFunction;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return this.width;
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return this.height;
    }

}
