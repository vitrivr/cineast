package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ARPartioner;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorARP44Normalized extends AbstractFeatureModule {

	public MedianColorARP44Normalized(){
		super("features_MedianColorARP44Normalized", 115854f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			MultiImage median = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
			FloatVector vec = ARPartioner.partitionImage(median, 4, 4).first;
			persist(shot.getId(), vec);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(ImageHistogramEqualizer.getEqualized(sc.getMedianImg()), 4, 4);
		return getSimilar(p.first.toArray(null), new QueryConfig(qc).setDistanceWeights(p.second));
	}

}
