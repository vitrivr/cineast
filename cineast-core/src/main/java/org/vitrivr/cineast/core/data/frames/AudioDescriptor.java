package org.vitrivr.cineast.core.data.frames;

/**
 * The class encapsulates descriptive information concerning an audio-stream that does not change between frames. The intention behind this class is that {@link AudioFrame}s that belong together share the same instance of the AudioDescriptor.
 *
 * @param samplingrate Samplingrate of the audio associated with this descriptor.
 * @param channels     Number of channels in the audio associated with this descriptor.
 * @param duration     Duration of the audio associated with this descriptor in milliseconds.
 */
public record AudioDescriptor(float samplingrate, int channels, long duration) {

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
