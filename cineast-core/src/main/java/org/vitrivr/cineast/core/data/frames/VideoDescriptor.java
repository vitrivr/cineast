package org.vitrivr.cineast.core.data.frames;

/**
 * The class encapsulates descriptive information concerning a video-stream (visual only) that does not change between frames. The intention behind this
 * class is that {@link VideoFrame}s that belong together share the same instance of the AudioDescriptor.
 *
 * @author rgasser
 * @version 1.0
 * @created 28.04.17
 */
public class VideoDescriptor {
    /** Frame rate of the video associated with this descriptor. */
    private final float fps;

    /** Duration of the video associated with this descriptor in milliseconds. */
    private final long duration;

    /** Width of the video associated with this descriptor. */
    private final int width;

    /** Height of the video associated with this descriptor. */
    private final int height;

    /**
     * Constructor for VideoDescriptor
     *
     * @param fps
     * @param duration
     * @param width
     * @param height
     */
    public VideoDescriptor(float fps, long duration, int width, int height) {
        this.fps = fps;
        this.duration = duration;
        this.width = width;
        this.height = height;
    }

    /**
     * Getter for fps.
     *
     * @return
     */
    public float getFps() {
        return fps;
    }

    /**
     * Getter for duration.
     *
     * @return
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Getter for width.
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Getter for height
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

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
