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

public class AverageColorGrid8Normalized extends AverageColorGrid8 {

	public AverageColorGrid8Normalized(){
		super("features.AverageColorGrid8Normalized", "grid", 12595f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(qc.getAvgImg()));
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorGrid8Normalized USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(qc.getAvgImg()));
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + "SELECT * FROM features.AverageColorGrid8, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if (!phandler.check("SELECT * FROM features.AverageColorGrid8Normalized WHERE shotid = " + shot.getId())) {
			MultiImage avgimg = ImageHistogramEqualizer.getEqualized(shot.getAvgImg());
			
			addToDB(shot.getId(), partition(avgimg).first);
		}
	}
}
