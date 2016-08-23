package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.MotionHistogramCalculator;

public class SubDivMotionSum4 extends MotionHistogramCalculator {

	
	public SubDivMotionSum4() {
		super("features_SubDivMotionHistogram4", 100 * 16);
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(4, qc.getPaths());
//
//		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
//		
//		ResultSet rset = this.selector.select("SELECT * FROM features.SubDivMotionHistogram4 USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', sums) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(4, qc.getPaths());
//
//		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SubDivMotionHistogram4, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', sums) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	
}
