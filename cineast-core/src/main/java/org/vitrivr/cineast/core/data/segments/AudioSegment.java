package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This AudioSegment is part of the Cineast data model and can hold an arbitrary number of AudioFrames that somehow
 * belong together. The class itself is agnostic to how segmenting is organized.
 *
 * The AudioSegment implements the SegmentContainer interface and provides access to different, frames-related data.
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

    /** Total duration of the AudioSegment in seconds. */
    private float totalDuration;

    /**
     * The AudioDescriptor that describes the audio in this AudioSegment. It is set by the first frame that is added to the
     * segment and must be equal for all the following frames.
     */
    private AudioDescriptor descriptor;

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
    @Override
    public List<AudioFrame> getAudioFrames() {
        return Collections.unmodifiableList(this.frames);
    }

    /**
     * Adds an AudioFrame to the collection of frames and thereby increases both
     * the number of frames and the duration of the segment.
     *
     * @param frame AudioFrame to add.
     * @return boolean True if frame was added, false otherwise.
     */
    public boolean addFrame(AudioFrame frame) {
        if (frame == null) {
          return false;
        }
        if (this.descriptor == null) {
          this.descriptor = frame.getDescriptor();
        }
        if (!this.descriptor.equals(frame.getDescriptor())) {
          return false;
        }

        this.totalSamples += frame.numberOfSamples();
        this.totalDuration += frame.getDuration();
        this.frames.add(frame);

        return true;
    }

    /**
     * Getter for the total number of samples in the AudioSegment.
     *
     * @return
     */
    @Override
    public int getNumberOfSamples() {
        return this.totalSamples;
    }

    /**
     * Getter for the total duration of the AudioSegment.
     *
     * @return
     */
    @Override
    public float getAudioDuration() {
        return totalDuration;
    }

    /**
     *
     * @return
     */
    @Override
    public float getSamplingrate() {
        return this.descriptor.getSamplingrate();
    }

    /**
     *
     * @return
     */
    @Override
    public int getChannels() {
        return this.descriptor.getChannels();
    }

    /**
     * Getter for the frame-number of the start-frame.
     *
     * @return
     */
    @Override
    public int getStart(){
        if (!this.frames.isEmpty()) {
            return (int)this.frames.get(0).getIdx();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the frame-number of the end-frame.
     *
     * @return
     */
    @Override
    public int getEnd(){
        if (!this.frames.isEmpty()) {
            return (int)this.frames.get(this.frames.size()-1).getIdx();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the start (in seconds) of this segment.
     *
     * @return
     */
    @Override
    public float getAbsoluteStart(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(0).getStart();
        } else {
            return 0;
        }
    }

    /**
     * Getter for the end (in seconds) of this segment.
     *
     * @return
     */
    @Override
    public float getAbsoluteEnd(){
        if (!this.frames.isEmpty()) {
            return this.frames.get(this.frames.size()-1).getEnd();
        } else {
            return 0;
        }
    }

    /**
     * Calculates and returns the Short-term Fourier Transform of the
     * current AudioSegment.
     *
     * @param windowsize Size of the window used during STFT. Must be a power of two.
     * @param overlap Overlap in samples between two subsequent windows.
     * @param padding Zero-padding before and after the actual sample data. Causes the window to contain (windowsize-2*padding) data-points..
     * @param function WindowFunction to apply before calculating the STFT.
     *
     * @return STFT of the current AudioSegment or null if the segment is empty.
     */
    @Override
    public STFT getSTFT(int windowsize, int overlap, int padding, WindowFunction function) {
        if (2*padding >= windowsize) {
          throw new IllegalArgumentException("The combined padding must be smaller than the sample window.");
        }
        STFT stft = new STFT(windowsize, overlap, padding, function, this.descriptor.getSamplingrate());
        stft.forward(this.getMeanSamplesAsDouble());
        return stft;
    }
}
