package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;

import java.util.Arrays;

/**
 * @author rgasser
 * @version 1.0
 * @created 03.02.17
 */
public interface AudioSTFTProvider {
    /**
     * Calculates and returns the Short-term Fourier Transform of the
     * current AudioSegment.
     *
     * @param windowsize Size of the window used during STFT. Must be a power of two.
     * @param overlap Overlap in samples between two subsequent windows.
     * @param function WindowFunction to apply before calculating the STFT.
     *
     * @return STFT of the current AudioSegment.
     */
    default STFT getSTFT(int windowsize, int overlap, WindowFunction function) {
       return this.getSTFT(windowsize, overlap, 0, function);
    }

    /**
     * Calculates and returns the Short-term Fourier Transform of the
     * current AudioSegment.
     *
     * @param windowsize Size of the window used during STFT. Must be a power of two.
     * @param overlap Overlap in samples between two subsequent windows.
     * @param function WindowFunction to apply before calculating the STFT.
     *
     * @return STFT of the current AudioSegment.
     */
    default STFT getSTFT(int windowsize, int overlap, int padding, WindowFunction function) {
        double[] data = new double[windowsize];
        Arrays.fill(data, 0.0);
        STFT stft = new STFT(windowsize,overlap,padding,function, 22050);
        stft.forward(data);
        return stft;
    }
}
