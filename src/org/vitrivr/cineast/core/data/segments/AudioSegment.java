package org.vitrivr.cineast.core.data.segments;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.audio.AudioFrame;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

import java.util.*;

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

    /** Total duration of the AudioSegment in seconds. */
    private float totalDuration;

    /** Sample rate of the AudioSegment. Determined by the sample rate of the first AudioFrame. */
    private Float samplerate;

    /** Number of channels in the AudioSegment. Determined by the number of channels in the first AudioFrame. */
    private Integer channels;

    /** */
    private Map<String,STFT> stft = new HashMap<>();

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
        if (frame == null) return;
        if (this.channels == null) this.channels = frame.getChannels();
        if (this.samplerate == null) this.samplerate = frame.getSampleRate();

        if (this.channels != frame.getChannels() || this.samplerate != frame.getSampleRate()) {
            return;
        }

        this.totalSamples += frame.numberOfSamples();
        this.totalDuration += frame.getDuration();
        this.frames.add(frame);
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
     *
     * @return
     */
    public float getSampleRate() {
        return this.samplerate;
    }

    /**
     *
     * @return
     */
    public int getChannels() {
        return this.channels;
    }

    /**
     * Getter for the frame-number of the start-frame.
     *
     * @return
     */
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
    public int getEnd(){
        if (!this.frames.isEmpty()) {
            return (int)this.frames.get(this.frames.size()-1).getIdx();
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

    /**
     * Calculates and returns the Short-term Fourier Transform of the
     * current AudioSegment.
     *
     * @param windowsize
     * @param overlap
     * @param function
     *
     * @return STFT of the current AudioSegment.
     */
    @Override
    public STFT getSTFT(int windowsize, int overlap, WindowFunction function) {
       STFT stft = new STFT(this.getMeanSamplesAsDouble(), this.samplerate);
       stft.forward(windowsize, overlap, function);
       return stft;
    }
}
