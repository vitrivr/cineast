package org.vitrivr.cineast.core.data.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

/**
 * Represents a single audio-frame containing a specific number of samples (the number depends on the decoder that
 * created the AudioFrame). Sample data is stored in a byte array and internally represented as signed, interleaved
 * 16bit PCM i.e. each sample is represented by a signed 16bit integer value (= short).
 *
 * The AudioFrame class supports different sample-rates and an arbitrary number of samples and is compatible with
 * the Java Audio API.
 *
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public class AudioFrame {

    /** Default empty audio frame. Encodes a single, mute sample for one channel. */
    public final static AudioFrame EMPTY_FRAME = new AudioFrame(22050, 1, new byte[2]);

    /** ByteBuffer holding the raw 16bit PCM data. */
    private final ByteBuffer data;

    /** Sample rate of this AudioFrame. */
    private final int sampleRate;

    /** Number of channels in this AudioFrame. */
    private final int channels;

    /**
     *
     * @param sampleRate
     * @param data
     */
    public AudioFrame(int sampleRate, int channels, byte[] data){
        this.data = ByteBuffer.wrap(data);
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    /**
     * Returns a Java AudioFormat object that specifies the arrangement of the audio-data in the
     * current AudioFrame.
     *
     * @return AudioFormat
     */
    public AudioFormat getFormat() {
        return new AudioFormat(this.sampleRate, 16, this.channels, true, false);
    }

    /**
     * Returns the size of the audio data in bytes.
     *
     * @return
     */
    public int getByteLength() {
        return this.data.array().length;
    }

    /**
     * Returns the total number of samples per channel in this AudioFrame.
     *
     * @return
     */
    public int count() {
        return this.data.array().length/(2 * this.channels);
    }

    /**
     * Getter for the raw byte array.
     *
     * @return Byte array containing the audio data of this AudioFrame.
     */
    public byte[] getData() {
        return data.array();
    }

    /**
     * Getter for sample-rate.
     *
     * @return Sample rate of this AudioFrame.
     */
    public int getSampleRate() {
        return this.sampleRate;
    }

    /**
     * Getter for the number of channels.
     *
     * @return Number of channels in this AudioFrame.
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Returns the sample specified sample in the specified channel
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final short getSample(int idx, int channel) {
        if (channel < this.channels) {
            return this.data.getShort(idx * this.channels + channel);
        } else {
            throw new IllegalArgumentException("The channel indexed must not exceed the number of channels!");
        }
    }

    /**
     * Calculates and returns the mean sample value (across all channels)
     * at the specified sample index.
     *
     * @param idx Index of the sample (zero-based)
     * @return Mean value of the sample at the specified index.
     */
    public final short getMeanSample(int idx) {
        int meanSample = 0;
        for (int i=0;i<this.channels;i++) {
            meanSample += this.getSample(idx, i);
        }
        return (short)(meanSample/this.channels);
    }
}