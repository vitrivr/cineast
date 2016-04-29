package ch.unibas.cs.dbis.cineast.core.features;

import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorGrid8Normalized extends MedianColorGrid8 {

	public MedianColorGrid8Normalized(){
		super("features.MedianColorGrid8Normalized", "grid", 12595f / 4f);
	}
	
	
	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			MultiImage medimg = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
			
			persist(shot.getId(), partition(medimg).first);
		}
	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()));
//		FloatVector query = p.first;
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select("SELECT * FROM features.MedianColorGrid8Normalized USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}
}
