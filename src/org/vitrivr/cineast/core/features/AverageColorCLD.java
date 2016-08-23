package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorLayoutDescriptor;

public class AverageColorCLD extends AbstractFeatureModule {

	public AverageColorCLD(){
		super("features_AverageColorCLD", 1960f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(shot.getAvgImg());
			persist(shot.getId(), fv);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(sc.getAvgImg());
		return getSimilar(query.toArray(null), qc);
	}

}
