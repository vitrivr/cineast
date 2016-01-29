package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ARPartioner;

public class AverageColorARP44 extends AbstractFeatureModule {
	
	public AverageColorARP44(){
		super("features.AverageColorARP44", "arp", 115854f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(qc.getAvgImg(), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorARP44 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.AverageColorARP44 WHERE shotid = " + shot.getId())){
			Pair<FloatVector, float[]> p = ARPartioner.partitionImage(shot.getAvgImg(), 4, 4);
			addToDB(shot.getId(), p.first);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(qc.getAvgImg(), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.AverageColorARP44, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

}
