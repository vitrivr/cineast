package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public interface AvgImgProvider {

  /**
   * 
   * @return the aggregated pixel-wise average of multiple images. By default, the {@link MultiImage}.EMPTY_MULTIIMAGE is returned.
   */
	public default MultiImage getAvgImg(){
	  return MultiImage.EMPTY_MULTIIMAGE;
	}
	
}
