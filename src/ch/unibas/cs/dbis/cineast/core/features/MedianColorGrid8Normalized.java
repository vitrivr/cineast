package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorGrid8Normalized extends MedianColorGrid8 {

	public MedianColorGrid8Normalized(){
		super("features.MedianColorGrid8Normalized", "grid", 12595f / 4f);
	}
	
	
	@Override
	public void processShot(FrameContainer shot) {
		if (!phandler.check("SELECT * FROM features.MedianColorGrid8Normalized WHERE shotid = " + shot.getId())) {
			MultiImage medimg = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
			
			addToDB(shot.getId(), partition(medimg).first);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()));
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MedianColorGrid8Normalized USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
}
