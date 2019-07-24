package org.vitrivr.cineast.core.data.providers;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.data.frames.AudioFrame;

/**
 *
 * This interface should be implemented by segments that provides access to frames-data in the form of {@link AudioFrame}.
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
        final ArrayList<AudioFrame> list = new ArrayList<>();
        list.add(AudioFrame.EMPTY_FRAME);
        return list;
    }

    /**
     * Returns the raw samples in the specified channel as short array.
     *
     * @param channel The channel for which to return the frames-data (zero-based index).
     * @return short array containing the samples.
     */
    default short[] getSamplesAsShort(int channel) {
        short[] samples = new short[this.getNumberOfSamples()];
        int idx = 0;
        for (AudioFrame frame : this.getAudioFrames()) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getSampleAsShort(sample, channel);
            }
        }
        return samples;
    }

    /**
     * Returns the raw samples in the specified channel as double array.
     *
     * @param channel The channel for which to return the frames-data (zero-based index).
     * @return double array containing the samples.
     */
    default double[] getSamplesAsDouble(int channel) {
        double[] samples = new double[this.getNumberOfSamples()];
        int idx = 0;
        for (AudioFrame frame : this.getAudioFrames()) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getSampleAsDouble(sample, channel);
            }
        }
        return samples;
    }

    /**
     * Returns the mean samples across all channels as short array.
     *
     * @return short array containing the mean sample values.
     */
    default short[] getMeanSamplesAsShort() {
        short[] samples = new short[this.getNumberOfSamples()];
        int idx = 0;
        for (AudioFrame frame : this.getAudioFrames()) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getMeanSampleAsShort(sample);
            }
        }
        return samples;
    }

    /**
     * Returns the mean samples across all channels as double array.
     *
     * @return double array containing the mean sample values.
     */
    default double[] getMeanSamplesAsDouble() {
        double[] samples = new double[this.getNumberOfSamples()];
        int idx = 0;
        for (AudioFrame frame : this.getAudioFrames()) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getMeanSampleAsDouble(sample);
            }
        }
        return samples;
    }

    /**
     * Returns the total number of samples in the frames segment (i.e. across all frames).
     *
     * @return Total number of samples in the segments
     */
    default int getNumberOfSamples() {
        return AudioFrame.EMPTY_FRAME.numberOfSamples();
    }

    /**
     * Returns the total duration  in seconds of all samples in the frames segment
     * (i.e. across all frames).
     *
     * @return Total duration in seconds.
     */
    default float getAudioDuration() {
        return AudioFrame.EMPTY_FRAME.getDuration();
    }

    /**
     * Returns the samplingrate of the segment. That rate usually determined by the first AudioFrame
     * added to the segment and must be the same for all frames.
     *
     * @return Sampling rate of the frames segment.
     */
    default float getSamplingrate() {
        return AudioFrame.EMPTY_FRAME.getSamplingrate();
    }

    /**
     * Returns the number of channels for the frames segment. Is usually determined by
     * the first AudioFrame added to the segment and must be the same for all frames.
     *
     * @return Number of channels of the frames segment.
     */
    default int getChannels() {
        return AudioFrame.EMPTY_FRAME.getChannels();
    }
}
