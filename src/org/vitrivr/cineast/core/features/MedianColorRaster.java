package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;

public class MedianColorRaster extends AverageColorRaster {

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("features_MedianColorRaster");
		this.phandler.setFieldNames("id", "raster", "hist");
	}

	@Override
	MultiImage getMultiImage(SegmentContainer shot){
		return shot.getMedianImg();
	}
	
}
