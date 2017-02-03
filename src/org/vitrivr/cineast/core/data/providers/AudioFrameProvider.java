package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.audio.AudioFrame;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This interface should be implemented by segments that provides access to audio-data in the form of AudioFrames.
 *
 * Currently, one AudioFrame holds an arbitrary number of samples and channels (as returned by the respective decoder). An
 * audio-segment groups multiple such frames and the methods defined in this interface facilitate easy access to the
 * underlying data.
 *
 * @author rgasser
 * @version 1.0
 * @created 31.01.17
 */
public interface AudioFrameProvider {

    /**
     * Returns a list of audio-frames contained in the AudioSegment. The
     * default implementation returns a list containing one, empty frame.
     *
     * @return List auf audio-frames in the audio-segment.
     */
    default List<AudioFrame> getAudioFrames() {
        ArrayList<AudioFrame> list = new ArrayList<>();
        list.add(AudioFrame.EMPTY_FRAME);
        return list;
    }

    /**
     * Returns the raw samples in the specified channel as short array.
     *
     * @param channel The channel for which to return the audio-data (zero-based index).
     * @return short array containing the samples.
     */
    default short[] getSamplesAsShort(int channel) {
        short[] data = new short[1];
        data[0] = AudioFrame.EMPTY_FRAME.getSampleAsShort(0,0);
        return data;
    }

    /**
     * Returns the raw samples in the specified channel as double array.
     *
     * @param channel The channel for which to return the audio-data (zero-based index).
     * @return double array containing the samples.
     */
    default double[] getSamplesAsDouble(int channel) {
        double[] data = new double[1];
        data[0] = AudioFrame.EMPTY_FRAME.getSampleAsDouble(0,0);
        return data;
    }

    /**
     * Returns the mean samples across all channels as short array.
     *
     * @return short array containing the mean sample values.
     */
    default short[] getMeanSamplesAsShort() {
        short[] data = new short[1];
        data[0] = AudioFrame.EMPTY_FRAME.getSampleAsShort(0,0);
        return data;
    }

    /**
     * Returns the mean samples across all channels as double array.
     *
     * @return double array containing the mean sample values.
     */
    default double[] getMeanSamplesAsDouble() {
        double[] data = new double[1];
        data[0] = AudioFrame.EMPTY_FRAME.getSampleAsDouble(0,0);
        return data;
    }

    /**
     * Returns the total number of samples in the audio segment (i.e. accross
     * all frames).
     *
     * @return Total number of samples in the segments
     */
    default int getNumberOfSamples() {
        return 1;
    }

    /**
     * Returns the total duration  in seconds of all samples in the audio segment
     * (i.e. accross all frames).
     *
     * @return Total duration in seconds.
     */
    default float getDuration() {
        return AudioFrame.EMPTY_FRAME.getDuration();
    }
}
