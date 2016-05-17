package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ARPartioner;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

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
		return getSimilar(p.first.toArray(null), qc);
	}

}
