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
 */
public class AudioFrame {
    /** Default empty frames frame. Encodes a single, mute sample for one channel. */
    public final static AudioFrame EMPTY_FRAME = new AudioFrame(0,0, new byte[0], new AudioDescriptor(22050, 1, 0));

    /** Number of bits in a sample. */
    public final static int BITS_PER_SAMPLE = 16;

    /** Index of the AudioFrame usually generated in the decoding context (e.g. i-th frame of the decoded file). */
    private final long idx;

    /** Timestamp in milliseconds of the first sample in the AudioFrame, relative to the whole file. */
    private final long timestamp;

    /** AudioDescriptor that describes the audio in this frame. */
    private final AudioDescriptor descriptor;

    /** Number of samples per channel in this AudioFrame. */
    private int numberOfSamples;

    /** ByteBuffer holding the raw 16bit int data. */
    private ByteBuffer data;

    /**
     * Default constructor.
     *
     * @param idx Index of the first sample (pair) in the AudioFrame.
     * @param timestamp Index of the first sample.
     * @param descriptor AudioDescriptor for the stream this frame stems from.
     * @param data Byte array containing 16bit signed PCM data.
     */
    public AudioFrame(long idx, long timestamp, byte[] data, AudioDescriptor descriptor) {
        this.idx = idx;
        this.descriptor = descriptor;
        this.timestamp = timestamp;
        this.setData(data);
    }

    public AudioFrame(AudioFrame other){
        this(other.idx, other.timestamp, other.data.array(), new AudioDescriptor(other.descriptor.getSamplingrate(), other.descriptor.getChannels(), other.descriptor.getDuration()));
    }

    /**
     * Returns a Java AudioFormat object that specifies the arrangement of the frames-data in the
     * current AudioFrame.
     *
     * @return AudioFormat
     */
    public final AudioFormat getFormat() {
        return new AudioFormat(this.descriptor.getSamplingrate(), BITS_PER_SAMPLE, this.descriptor.getChannels(), true, false);
    }

    /**
     *  Returns the AudioDescriptor associated with the this AudioFrame.
     *
     * @return AudioDescriptor
     */
    public final AudioDescriptor getDescriptor() {
        return this.descriptor;
    }

    /**
     * Returns the presentation timestamp of the first sample.
     *
     * @return Presentation timestamp pf the first sample.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns the size of the {@link AudioFrame} data in bytes.
     *
     * @return Size of {@link AudioFrame} in bytes.
     */
    public final int size() {
        return this.data.array().length;
    }

    /**
     * Returns the total number of samples per channel in this {@link AudioFrame}.
     *
     * @return Number of samples per channel.
     */
    public final int numberOfSamples() {
        return this.numberOfSamples;
    }

    /**
     * Getter for the frame id (counter).
     *
     * @return Sample id
     */
    public final long getIdx() {
        return this.idx;
    }

    /**
     * Getter for the raw byte array.
     *
     * @return Byte array containing the frames data of this AudioFrame.
     */
    public final byte[] getData() {
        return this.data.array();
    }

    /**
     * Getter for sample-rate.
     *
     * @return Sample rate of this AudioFrame.
     */
    public final float getSamplingrate() {
        return this.descriptor.getSamplingrate();
    }

    /**
     * Returns the duration of the {@link AudioFrame} in seconds.
     *
     * @return Duration of the {@link AudioFrame}
     */
    public final float getDuration() {
        return this.numberOfSamples/this.descriptor.getSamplingrate();
    }

    /**
     * Returns the relative start of the {@link AudioFrame} in seconds.
     *
     * @return Relative start of the {@link AudioFrame}
     */
    public final float getStart() {
        return this.timestamp/1000.0f;
    }

    /**
     * Returns the relative end of the AudioFrame in seconds.
     *
     * @return Relative end of the {@link AudioFrame}.
     */
    public final float getEnd() {
        return this.getStart() + this.numberOfSamples/this.descriptor.getSamplingrate();
    }

    /**
     * Getter for the number of channels.
     *
     * @return Number of channels in this AudioFrame.
     */
    public final int getChannels() {
        return this.descriptor.getChannels();
    }

    /**
     * Returns the sample specified sample in the specified channel as short value.
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final short getSampleAsShort(int idx, int channel) {
        if (channel < this.descriptor.getChannels()) {
            return this.data.getShort(2*idx * this.descriptor.getChannels() + 2*channel);
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
        for (int i=0;i<this.descriptor.getChannels();i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (short)(meanSample/this.descriptor.getChannels());
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
        for (int i=0;i<this.descriptor.getChannels();i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (meanSample/(this.descriptor.getChannels() * Short.MAX_VALUE));
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
        if (!this.descriptor.equals(that.descriptor)) {
          return false;
        }
        int bytes = that.descriptor.getChannels() * numberOfSamples * (BITS_PER_SAMPLE/8);
        if (bytes > that.data.capacity()) {
          return false;
        }

        /* Copy data. */
        byte[] data = new byte[this.data.capacity() + bytes];
        System.arraycopy(this.data.array(), 0, data, 0, this.data.capacity());
        System.arraycopy(that.data.array(), 0, data, this.data.capacity(), bytes);

        /* Update local ByteBuffer reference. */
        this.setData(data);
        return true;
    }

    /**
     * Appends the provided {@link AudioFrame} to the this {@link AudioFrame} and returns true, if the
     * operation was successful and false otherwise.
     *
     * @param that The {@link AudioFrame} that should be appended.
     * @return True on success, false otherwise.
     */
    public boolean append(AudioFrame that) {
        return this.append(that, that.numberOfSamples);
    }

    public AudioFrame split(int numberOfSamples){

        if (numberOfSamples > this.numberOfSamples){
            return this;
        }

        int bytesToCut = this.descriptor.getChannels() * numberOfSamples * (BITS_PER_SAMPLE/8);
        byte[] cutBytes = new byte[bytesToCut];
        byte[] remaining = new byte[this.data.capacity() - bytesToCut];

        System.arraycopy(this.data.array(), 0, cutBytes, 0, bytesToCut);
        System.arraycopy(this.data.array(), bytesToCut, remaining, 0, remaining.length);

        setData(remaining);

        return new AudioFrame(idx, timestamp, cutBytes, new AudioDescriptor(descriptor.getSamplingrate(), descriptor.getChannels(), (long) (numberOfSamples/descriptor.getSamplingrate())));
    }

    /**
     * Internal method to update the buffer holding the actual audio data.
     *
     * @param data Byte array with the samples.
     */
    private void setData(byte[] data) {
        this.data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.numberOfSamples = data.length/(2 * this.descriptor.getChannels());
    }
}