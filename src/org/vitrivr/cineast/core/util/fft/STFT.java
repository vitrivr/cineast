package org.vitrivr.cineast.core.util.fft;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.vitrivr.cineast.core.util.fft.windows.HanningWindow;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

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
    /** Reference to the samples for which the STFT should be calculated. */
    private final double[] samples;

    /** Sampling rate with which the samples have been sampled. */
    private final int samplingrate;

    /** Window function to use when calculating the FFT. */
    private WindowFunction windowFunction = new HanningWindow();

    /** Size of the FFT window. Must be a power of 2 (e.g. 512, 1024, 2048, 4096). */
    private int windowsize = 4096;

    /** Overlap in samples between two subsequent windows. */
    private int overlap = 128;

    /** Width of the STFT (i.e. the number of timepoints or FFT's). */
    private int width;

    /** Height of the STFT (i.e. the number of frequency bins per FFT). */
    private int height;

    /** Frequency labels in ascending order (for all FFTs). */
    private float[] frequencies;

    /** Time labels in ascending order (for each entry in the stft list). */
    private float[] time;

    /** List containing one FFT entry per timepoint. Same order as time[] */
    private List<FFT> stft;

    /** List containing the power spectrum of the FFT per timepoint. Lazily calculated! */
    private List <Spectrum> powerSpectrum;

    /** List containing the magnitude spectrum of the FFT per timepoint. Lazily calculated! */
    private List <Spectrum> maginitudeSpectrum;

    /**
     * @param samples
     * @param samplingrate
     */
    public STFT(double[] samples, int samplingrate) {
        /* Store the local variables. */
        this.samples = samples;
        this.samplingrate = samplingrate;
    }

    /**
     * @param windowsize
     * @param overlap
     * @param function
     */
    public void forward(int windowsize, int overlap, WindowFunction function) {
        /* Make sure that hte windowsize is a power of two. */
        if (!FFTUtil.isPowerOf2(windowsize)) throw new IllegalArgumentException("The provided window size of " + windowsize + " is not a power of two!");

        /* Store the window size and window function. */
        this.windowFunction = function;
        this.windowsize = windowsize;

        /* Derive properties from the available information. */
        this.width = (int) Math.ceil((float) this.samples.length / (windowsize - overlap));
        this.height = windowsize/2;
        this.frequencies = FFTUtil.binFrequencies(windowsize, samplingrate);
        this.time = FFTUtil.time(this.width, windowsize, samplingrate);
        this.stft = new ArrayList<>(this.width);
        this.powerSpectrum = null;
        this.maginitudeSpectrum = null;

        /* Prepare transformer class. */
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

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
            fft.forward(window, this.windowFunction);
            this.stft.add(fft);
        }
    }

    /**
     *
     * @return
     */
    public List<Spectrum> getPowerSpectrum() {
        if (this.powerSpectrum == null) {
            this.powerSpectrum = new ArrayList<>(this.stft.size());
            for (FFT fft : this.stft) {
                this.powerSpectrum.add(fft.getPowerSpectrum(this.samplingrate));
            }
        }
        return Collections.unmodifiableList(this.powerSpectrum);
    }

    /**
     *
     * @return
     */
    public List<Spectrum> getMaginitudeSpectrum() {
        if (this.maginitudeSpectrum == null) {
            this.maginitudeSpectrum = new ArrayList<>(this.stft.size());
            for (FFT fft : this.stft) {
                this.maginitudeSpectrum.add(fft.getMagnitudeSpectrum(this.samplingrate));
            }
        }
        return Collections.unmodifiableList(this.powerSpectrum);
    }

    /**
     * Getter for frequency labels.
     *
     * @return
     */
    public final float[] getFrequencies() {
        return frequencies;
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
