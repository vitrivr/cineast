package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorGrid8Normalized extends MedianColorGrid8 {

	public MedianColorGrid8Normalized(){
		super("features_MedianColorGrid8Normalized", 12595f / 4f);
	}
	
	
	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			MultiImage medimg = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
			
			persist(shot.getId(), partition(medimg).first);
		}
	}


	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(sc.getMedianImg()));
		return getSimilar(p.first.toArray(null), new QueryConfig(qc).setDistanceWeights(p.second));
	}

}
