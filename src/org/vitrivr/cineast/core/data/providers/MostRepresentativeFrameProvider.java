package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.Frame;

public interface MostRepresentativeFrameProvider {
  
  /**
   * 
   * @return the frame which best represents a sequence of frames. By default, the {@link Frame}.EMPTY_FRAME is returned
   */
	public default Frame getMostRepresentativeFrame(){
	  return Frame.EMPTY_FRAME;
	}
	
}
