package org.vitrivr.cineast.core.data.query.containers;

import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

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

    /** Total duration of the AudioSegment in seconds. */
    private float totalDuration;

    private float weight = 1.0f;

    /** Sample rate of the AudioSegment. Determined by the sample rate of the first AudioFrame. */
    private Float samplerate;

    /** Number of channels in the AudioSegment. Determined by the number of channels in the first AudioFrame. */
    private Integer channels;

    /**
     * Returns a list of audio-frames contained in the AudioSegment. The
     * default implementation returns a list containing one, empty frame.
     *
     * @return List auf audio-frames in the audio-segment.
     */
    public AudioQueryContainer(List<AudioFrame> frames) {
        this.frames = frames;
        for (AudioFrame frame : this.frames) {
            if (this.channels == null) this.channels = frame.getChannels();
            if (this.samplerate == null) this.samplerate = frame.getSampleRate();
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
        return null;
    }

    @Override
    public String getSuperId() {
        return null;
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
    public float getAudioDuration() {
        return totalDuration;
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
