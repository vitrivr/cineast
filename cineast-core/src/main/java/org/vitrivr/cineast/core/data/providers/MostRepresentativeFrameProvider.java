package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.frames.VideoFrame;

public interface MostRepresentativeFrameProvider {

  /**
   * @return the frame which best represents a sequence of frames. By default, the {@link VideoFrame}.EMPTY_FRAME is returned
   */
  default VideoFrame getMostRepresentativeFrame() {
    return VideoFrame.EMPTY_VIDEO_FRAME;
  }

  /**
   * @return The frame ID of the most representative @link VideoFrame}.
   */
  default int getMostRepresentativeFrameNumber() {
    return this.getMostRepresentativeFrame().getId();
  }

  /**
   * @return The timestamp in milliseconds of the most representative {@link VideoFrame}.
   */
  default long getMostRepresentativeFrameTimestamp() {
    return this.getMostRepresentativeFrame().getTimestamp();
  }

  /**
   * @return The timestamp in milliseconds of the most representative {@link VideoFrame}.
   */
  default float getMostRepresentativeFrameTimestampSeconds() {
    return this.getMostRepresentativeFrame().getTimestampSeconds();
  }
}
