package ch.unibas.cs.dbis.cineast.core.features;

import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;

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
