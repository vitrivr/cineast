package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public interface MedianImgProvider {

  /**
   * 
   * @return the aggregated pixel-wise median of multiple images. By default, the {@link MultiImage}.EMPTY_MULTIIMAGE is returned.
   */
	public default MultiImage getMedianImg(){
	  return MultiImage.EMPTY_MULTIIMAGE;
	}
	
}
