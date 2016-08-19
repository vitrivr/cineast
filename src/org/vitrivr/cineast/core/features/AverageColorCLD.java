package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorLayoutDescriptor;

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
