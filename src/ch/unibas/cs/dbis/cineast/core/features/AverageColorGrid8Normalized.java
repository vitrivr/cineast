package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class AverageColorGrid8Normalized extends AverageColorGrid8 {

	public AverageColorGrid8Normalized(){
		super("features.AverageColorGrid8Normalized", 12595f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			MultiImage avgimg = ImageHistogramEqualizer.getEqualized(shot.getAvgImg());
			
			persist(shot.getId(), partition(avgimg).first);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(sc.getAvgImg()));
		return getSimilar(p.first.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return super.getSimilar(shotId, qc);
	}
}
