package org.vitrivr.cineast.core.data.frames;


import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a single audio-frame containing a specific number of samples (the number depends on the decoder that
 * created the AudioFrame). Sample data is stored in a byte array and internally represented as 16bit int PCM i.e. each sample
 * is represented by a signed 16bit short between -32767 and 32767.
 *
 * The AudioFrame class supports different sample-rates and an arbitrary number of samples and is compatible with the
 * Java Audio API.
 *
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public class AudioFrame {

    /** Default empty frames frame. Encodes a single, mute sample for one channel. */
    public final static AudioFrame EMPTY_FRAME = new AudioFrame(0,0,22050, 1, new byte[2]);

    /** Number of bits in a sample. */
    public final static int BITS_PER_SAMPLE = 16;

    /** Index of the AudioFrame usually generated in the decoding context (e.g. i-th frame of the decoded file). */
    private final long idx;

    /** Timestamp in milliseconds of the first sample in the AudioFrame (relative to the whole file). */
    private final long starttimepstamp;

    /** Sample rate of this AudioFrame. */
    private final float sampleRate;

    /** Number of channels in this AudioFrame. */
    private final int channels;

    /** Number of samples per channel in this AudioFrame. */
    private int numberOfSamples;

    /** ByteBuffer holding the raw 16bit int data. */
    private ByteBuffer data;

    /**
     * Default constructor.
     *
     * @param idx Index of the first sample (pair) in the AudioFrame.
     * @param starttimestamp Index of the first sample.
     * @param sampleRate Sample-rate of the new AudioFrame.
     * @param channels Number of channels of the new AudioFrame.
     * @param data Byte array containing 16bit signed PCM data.
     */
    public AudioFrame(long idx, long starttimestamp, float sampleRate, int channels, byte[] data) {
        this.idx = idx;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.starttimepstamp = starttimestamp;
        this.setData(data);
    }

    /**
     * Returns a Java AudioFormat object that specifies the arrangement of the frames-data in the
     * current AudioFrame.
     *
     * @return AudioFormat
     */
    public final AudioFormat getFormat() {
        return new AudioFormat(this.sampleRate, BITS_PER_SAMPLE, this.channels, true, false);
    }

    /**
     * Returns the size of the frames data in bytes.
     *
     * @return
     */
    public final int size() {
        return this.data.array().length;
    }

    /**
     * Returns the total number of samples per channel in this AudioFrame.
     *
     * @return
     */
    public final int numberOfSamples() {
        return this.numberOfSamples;
    }

    /**
     *
     * @return
     */
    public final long getIdx() {
        return idx;
    }

    /**
     * Getter for the raw byte array.
     *
     * @return Byte array containing the frames data of this AudioFrame.
     */
    public final byte[] getData() {
        return data.array();
    }

    /**
     * Getter for sample-rate.
     *
     * @return Sample rate of this AudioFrame.
     */
    public final float getSampleRate() {
        return this.sampleRate;
    }

    /**
     * Returns the duration of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getDuration() {
        return this.numberOfSamples/this.sampleRate;
    }

    /**
     * Returns the relative start of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getStart() {
        return this.starttimepstamp/1000.0f;
    }

    /**
     * Returns the relative end of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getEnd() {
        return this.getStart() + this.numberOfSamples/this.sampleRate;
    }

    /**
     * Getter for the number of channels.
     *
     * @return Number of channels in this AudioFrame.
     */
    public final int getChannels() {
        return channels;
    }

    /**
     * Returns the sample specified sample in the specified channel as short value.
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final short getSampleAsShort(int idx, int channel) {
        if (channel < this.channels) {
            return this.data.getShort(2*idx * this.channels + 2*channel);
        } else {
            throw new IllegalArgumentException("The channel indexed must not exceed the number of channels!");
        }
    }

    /**
     * Returns the sample specified sample in the specified channel as double
     * value between -1.0 and 1.0.
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final double getSampleAsDouble(int idx, int channel) {
        return ((double)this.getSampleAsShort(idx,  channel)/(double)Short.MAX_VALUE);
    }

    /**
     * Calculates and returns the mean sample value (across all channels)
     * at the specified sample index and returns it as short value.
     *
     * @param idx Index of the sample (zero-based)
     * @return Mean value of the sample at the specified index.
     */
    public final short getMeanSampleAsShort(int idx) {
        int meanSample = 0;
        for (int i=0;i<this.channels;i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (short)(meanSample/this.channels);
    }

    /**
     * Calculates and returns the mean sample value (across all channels) at the
     * specified sample index and returns it as double value between -1.0 and 1.0
     *
     * @param idx Index of the sample (zero-based)
     * @return Mean value of the sample at the specified index as float.
     */
    public final double getMeanSampleAsDouble(int idx) {
        float meanSample = 0;
        for (int i=0;i<this.channels;i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (meanSample/(this.channels * Short.MAX_VALUE));
    }

    /**
     * Appends an AudioFrame to the current AudioFrame if the two frames have the same specs in
     * terms of sampleRate and number of channels. The raw bytes of the other AudioFrame are
     * appended to the byte-array of the current AudioFrame.
     *
     * @param that The AudioFrame to append to the current frame.
     * @param numberOfSamples The number of samples to append. Must be smaller than the size of the other AudioFrame!
     * @return true if appending was successful, false otherwise.
     */
    public boolean append(AudioFrame that, int numberOfSamples) {
        if (this.sampleRate != that.sampleRate && this.channels != that.channels) return false;
        int bytes = that.channels * numberOfSamples * (BITS_PER_SAMPLE/8);
        if (bytes > that.data.capacity()) return false;

        /* Copy data. */
        byte[] data = new byte[this.data.capacity() + bytes];
        System.arraycopy(this.data.array(), 0, data, 0, this.data.capacity());
        System.arraycopy(that.data.array(), 0, data, this.data.capacity(), bytes);

        /* Update local ByteBuffer reference. */
        this.setData(data);
        return true;
    }

    /**
     *
     * @param that
     * @return
     */
    public boolean append(AudioFrame that) {
        return this.append(that, that.numberOfSamples);
    }

    /**
     *
     * @param data
     */
    private void setData(byte[] data) {
        this.data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.numberOfSamples = data.length/(2 * this.channels);
    }

}