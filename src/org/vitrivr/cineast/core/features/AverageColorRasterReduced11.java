package org.vitrivr.cineast.core.features;

import java.util.function.Supplier;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

public class AverageColorRasterReduced11 extends AverageColorRaster {

	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.phandler = supply.get();
		this.phandler.open("features_AverageColorRasterReduced11");
		this.phandler.setFieldNames("id", "raster", "hist");
	}

	@Override
	MultiImage getMultiImage(SegmentContainer shot){
		return ColorReductionUtil.quantize11(shot.getAvgImg());
	}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().createFeatureEntity("features_AverageColorRasterReduced11", true, "hist", "raster");
	}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().dropEntity("features_AverageColorRasterReduced11");
	}
}
