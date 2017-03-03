package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.frames.VideoFrame;

public interface MostRepresentativeFrameProvider {
  
  /**
   * 
   * @return the frame which best represents a sequence of frames. By default, the {@link VideoFrame}.EMPTY_FRAME is returned
   */
	public default VideoFrame getMostRepresentativeFrame(){
	  return VideoFrame.EMPTY_VIDEO_FRAME;
	}
	
}
