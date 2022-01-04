package org.vitrivr.cineast.core.data.providers;

import java.util.ArrayList;
import java.util.List;
import org.vitrivr.cineast.core.data.frames.VideoFrame;


public interface FrameListProvider {

  default List<VideoFrame> getVideoFrames() {
    ArrayList<VideoFrame> list = new ArrayList<>(1);
    list.add(VideoFrame.EMPTY_VIDEO_FRAME);
    return list;
  }
}
