package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.frames.VideoFrame;

import java.util.ArrayList;
import java.util.List;


public interface FrameListProvider {
    default List<VideoFrame> getVideoFrames() {
        ArrayList<VideoFrame> list = new ArrayList<>(1);
        list.add(VideoFrame.EMPTY_VIDEO_FRAME);
        return list;
    }
}
