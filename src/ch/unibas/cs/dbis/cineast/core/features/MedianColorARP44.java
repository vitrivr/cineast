package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ARPartioner;

public class MedianColorARP44 extends AbstractFeatureModule {

	
	private static final Logger LOGGER = LogManager.getLogger();

	public MedianColorARP44(){
		super("features.MedianColorARP44", "arp", 115854f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(qc.getMedianImg(), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MedianColorARP44 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(qc.getMedianImg(), 4, 4);
		FloatVector query = p.first;
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.MedianColorARP44, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', arp) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if(!phandler.check("SELECT * FROM features.MedianColorARP44 WHERE shotid = " + shot.getId())){
			MultiImage median = shot.getMedianImg();
			FloatVector vec = ARPartioner.partitionImage(median, 4, 4).first;
			addToDB(shot.getId(), vec);
		}
		LOGGER.exit();
	}

}
