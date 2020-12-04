package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

import java.util.function.Supplier;

public class MedianColorRaster extends AverageColorRaster {

	@Override
	public void init(PersistencyWriterSupplier supply, int batchSize) {
		/* TODO: Respect batchSize. */
		this.phandler = supply.get();
		this.phandler.open("features_MedianColorRaster");
		this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, "raster", "hist");
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
