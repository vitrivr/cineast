package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

import java.util.function.Supplier;

public class AverageColorRasterReduced11 extends AverageColorRaster {

	@Override
	public void init(PersistencyWriterSupplier supply, int batchSize) {
		/* TODO: Respect batchSize. */
		this.phandler = supply.get();
		this.phandler.open("features_AverageColorRasterReduced11");
		this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, "raster", "hist");
	}

	@Override
	MultiImage getMultiImage(SegmentContainer shot){
		return ColorReductionUtil.quantize11(shot.getAvgImg());
	}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().createFeatureEntity("features_AverageColorRasterReduced11", true, new AttributeDefinition("hist", AttributeType.VECTOR, 15), new AttributeDefinition("raster", AttributeType.VECTOR, 64));
	}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().dropEntity("features_AverageColorRasterReduced11");
	}
}
