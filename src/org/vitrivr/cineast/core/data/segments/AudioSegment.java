package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.audio.AudioFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This AudioSegment is part of the Cineast data model and can hold an arbitrary number of AudioFrames that somehow
 * belong together. The class itself is agnostic to how segmenting is organized.
 *
 * The AudioSegment implements the SegmentContainer interface and provides access to different, audio-related data.
 *
 *
 * @TODO:
 * - Perform basic checks when adding an audio-frame (sample-rate, duration...)
 *
 * @author rgasser
 * @version 1.0
 * @created 31.01.17
 */
public class AudioSegment implements SegmentContainer {
    /** Segment ID of the AudioSegment. */
    private String segmentId;

    /** ID of the multimedia object this AudioSegment belongs to. */
    private String objectId;

    /** List of AudioFrames in the AudioSegment. */
    private final List<AudioFrame> frames = new ArrayList<>();

    /** Total number of samples in the AudioSegment. */
    private int totalSamples;

    /** Total duration in secdons of the AudioSegment. */
    private float totalDuration;

    /**
     * @return a unique id of this
     */
    @Override
    public String getId() {
        return this.segmentId;
    }

    /**
     * @param id
     * @return a unique id of this
     */
    @Override
    public void setId(String id) {
        this.segmentId = id;
    }

    /**
     *
     * @return
     */
    @Override
    public String getSuperId() {
        return this.objectId;
    }

    /**
     * @param id
     */
    @Override
    public void setSuperId(String id) {
        this.objectId = id;
    }

    /**
     * Getter for the list of AudioFrames.
     *
     * @return Returns an unmodifiable list of audio-frames.
     */
    public List<AudioFrame> getAudioFrames() {
        return Collections.unmodifiableList(this.frames);
    }

    /**
     * Adds an AudioFrame to the collection of frames and thereby increases both
     * the number of frames and the duration of the segment.
     *
     * @param frame AudioFrame to add.
     */
    public void addFrame(AudioFrame frame) {
        if (frame != null) {
            this.totalSamples += frame.numberOfSamples();
            this.totalDuration += frame.getDuration();
            this.frames.add(frame);
        }
    }


    /**
     * Returns the raw samples in the specified channel as short array.
     *
     * @return short array containing the samples.
     */
    public short[] getSamplesAsShort(int channel) {
        short[] samples = new short[this.totalSamples];
        int idx = 0;
        for (AudioFrame frame : this.frames) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getSampleAsShort(sample, channel);
            }
        }
        return samples;
    }

    /**
     * Returns the raw samples in the specified channel as double array.
     *
     * @param channel The channel for which to return the audio-data (zero-based index).
     * @return double array containing the samples.
     */
    public double[] getSamplesAsDouble(int channel) {
        double[] samples = new double[this.totalSamples];
        int idx = 0;
        for (AudioFrame frame : this.frames) {
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
    public short[] getMeanSamplesAsShort() {
        short[] samples = new short[this.totalSamples];
        int idx = 0;
        for (AudioFrame frame : this.frames) {
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
    public double[] getMeanSamplesAsDouble() {
        double[] samples = new double[this.totalSamples];
        int idx = 0;
        for (AudioFrame frame : this.frames) {
            for (int sample = 0; sample < frame.numberOfSamples(); sample++, idx++) {
                samples[idx] = frame.getMeanSampleAsDouble(sample);
            }
        }
        return samples;
    }

    /**
     * Getter for the total number of samples in the AudioSegment.
     *
     * @return
     */
    public int getNumberOfSamples() {
        return this.totalSamples;
    }

    /**
     * Getter for the total duration of the AudioSegment.
     *
     * @return
     */
    public float getDuration() {
        return totalDuration;
    }

    /**
     * Getter for the frame-number of the start-frame.
     *
     * @return
     */
    public int getStart(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(0).getId();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the frame-number of the end-frame.
     *
     * @return
     */
    public int getEnd(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(this.frames.size()-1).getId();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the start (in seconds) relative to the audio-file this
     * segment belongs to.
     *
     * @return
     */
    public float getRelativeStart(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(0).getStart();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the end (in seconds) relative to the audio-file this
     * segment belongs to.
     *
     * @return
     */
    public float getRelativeEnd(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(this.frames.size()-1).getEnd();
        } else {
            return 0;
        }
    }
}
