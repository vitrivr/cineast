package org.vitrivr.cineast.core.data.frames;

/**
 * The class encapsulates descriptive information concerning a video-stream (visual only) that does not change between frames. The intention behind this class is that {@link VideoFrame}s that belong together share the same instance of the AudioDescriptor.
 *
 * @param fps      Frame rate of the video associated with this descriptor.
 * @param duration Duration of the video associated with this descriptor in milliseconds.
 * @param width    Width of the video associated with this descriptor.
 * @param height   Height of the video associated with this descriptor.
 */
public record VideoDescriptor(float fps, long duration, int width, int height) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    VideoDescriptor that = (VideoDescriptor) o;

    if (Float.compare(that.fps, fps) != 0) {
      return false;
    }
    if (duration != that.duration) {
      return false;
    }
    if (width != that.width) {
      return false;
    }
    return height == that.height;
  }

  @Override
  public int hashCode() {
    int result = (fps != +0.0f ? Float.floatToIntBits(fps) : 0);
    result = 31 * result + (int) (duration ^ (duration >>> 32));
    result = 31 * result + width;
    result = 31 * result + height;
    return result;
  }
}
