package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ARPartioner;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorARP44Normalized extends AbstractFeatureModule {

	public MedianColorARP44Normalized(){
		super("features.MedianColorARP44Normalized", "arp", 115854f / 4f);
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.MedianColorARP44Normalized WHERE shotid = " + shot.getId())){
			MultiImage median = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
			FloatVector vec = ARPartioner.partitionImage(median, 4, 4).first;
			addToDB(shot.getId(), vec);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MedianColorARP44Normalized USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.MedianColorARP44Normalized, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

}
