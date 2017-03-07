package org.vitrivr.cineast.core.data.query.containers;

import org.vitrivr.cineast.core.data.audio.AudioFrame;
import org.vitrivr.cineast.core.util.MathHelper;

import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 08.02.17
 */
public class AudioQueryContainer implements QueryContainer {

    /**
     *
     */
    private final List<AudioFrame> frames;

    /** Total number of samples in the AudioSegment. */
    private int totalSamples;

    /** Total duration in secdons of the AudioSegment. */
    private float totalDuration;

    private float weight;

    /**
     * Returns a list of audio-frames contained in the AudioSegment. The
     * default implementation returns a list containing one, empty frame.
     *
     * @return List auf audio-frames in the audio-segment.
     */
    public AudioQueryContainer(List<AudioFrame> frames) {
        this.frames = frames;
        for (AudioFrame frame : this.frames) {
            this.totalSamples += frame.numberOfSamples();
            this.totalDuration += frame.getDuration();
        }
    }

    /**
     *
     * @return
     */
    public List<AudioFrame> getAudioFrames() {
        return this.frames;
    }

    /**
     * @return a unique id of this
     */
    @Override
    public String getId() {
        return "1";
    }

    @Override
    public String getSuperId() {
        return "1";
    }

    /**
     * @param id
     * @return a unique id of this
     */
    @Override
    public void setId(String id) {

    }

    /**
     * @param id
     */
    @Override
    public void setSuperId(String id) {

    }

    /**
     *
     * @return
     */
    public float getWeight(){
        return this.weight;
    }

    /**
     *
     * @param weight
     */
    public void setWeight(float weight){
        if(Float.isNaN(weight)){
            this.weight = 0f;
            return;
        }
        this.weight = MathHelper.limit(weight, -1f, 1f);
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
}
