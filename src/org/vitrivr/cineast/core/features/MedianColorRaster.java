package org.vitrivr.cineast.core.features;

import java.util.function.Supplier;

import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.setup.EntityCreator;

public class MedianColorRaster extends AverageColorRaster {

	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.phandler = supply.get();
		this.phandler.open("features_MedianColorRaster");
		this.phandler.setFieldNames("id", "raster", "hist");
	}

	@Override
	MultiImage getMultiImage(SegmentContainer shot){
		return shot.getMedianImg();
	}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().createFeatureEntity("features_MedianColorRaster", true, new AttributeDefinition("hist", AttributeType.VECTOR, 15), new AttributeDefinition("raster", AttributeType.VECTOR, 64));
	}
	
}
