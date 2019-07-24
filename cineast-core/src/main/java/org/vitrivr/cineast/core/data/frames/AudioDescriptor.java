package org.vitrivr.cineast.core.data.frames;

/**
 * The class encapsulates descriptive information concerning an audio-stream that does not change between frames. The intention behind this
 * class is that {@link AudioFrame}s that belong together share the same instance of the AudioDescriptor.
 *
 * @author rgasser
 * @version 1.0
 * @created 28.04.17
 */
public class AudioDescriptor {
    /** Samplingrate of the audio associated with this descriptor. */
    private final float samplingrate;

    /** Number of channels in the audio associated with this descriptor. */
    private final int channels;

    /** Duration of the audio associated with this descriptor in milliseconds. */
    private final long duration;

    /**
     * Constructor for an AudioDescriptor.
     *
     * @param samplingrate
     * @param channels
     * @param duration
     */
    public AudioDescriptor(float samplingrate, int channels, long duration) {
        this.samplingrate = samplingrate;
        this.channels = channels;
        this.duration = duration;
    }

    /**
     * Getter for the samplingrate.
     *
     * @return Samplingrate of the source stream.
     */
    public final float getSamplingrate() {
        return this.samplingrate;
    }

    /**
     * Getter for channels.
     *
     * @return Number of channels in the source stream
     */
    public final int getChannels() {
        return this.channels;
    }

    /**
     * Getter for duration.
     *
     * @return Duration of the total source stream
     */
    public final long getDuration() {
        return this.duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }

        AudioDescriptor that = (AudioDescriptor) o;

        if (Float.compare(that.samplingrate, samplingrate) != 0) {
          return false;
        }
        if (channels != that.channels) {
          return false;
        }
        return duration == that.duration;
    }

    @Override
    public int hashCode() {
        int result = (samplingrate != +0.0f ? Float.floatToIntBits(samplingrate) : 0);
        result = 31 * result + channels;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        return result;
    }
}
