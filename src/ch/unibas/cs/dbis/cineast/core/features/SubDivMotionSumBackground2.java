package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.MotionHistogramCalculator;

public class SubDivMotionSumBackground2 extends MotionHistogramCalculator {

	public SubDivMotionSumBackground2() {
		super("features.SubDivMotionHistogramBackground2", "sums", 400);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(2, qc.getBgPaths());

		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
		
		ResultSet rset = this.selector.select("SELECT * FROM features.SubDivMotionHistogramBackground2 USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', sums) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(2, qc.getBgPaths());

		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SubDivMotionHistogramBackground2, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', sums) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}



}
