package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.MotionHistogramCalculator;
@Deprecated
public class MotionSum extends MotionHistogramCalculator {

	public MotionSum() {
		super("features_motionhistogram", 1);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getPaths());
//
//		double sum = pair.first.get(0);
//		
//		ResultSet rset = this.selector.select("SELECT shotid , pow(sum - " + sum + ", 2) as dist FROM features.MotionHistogram ORDER BY dist ASC LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select("WITH q AS (SELECT sum FROM features.motionhistogram WHERE shotid = " + shotId + ") SELECT pow(q.sum - motionhistogram.sum, 2) as dist, shotid FROM features.motionhistogram, q  ORDER BY dist ASC LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getPaths());
//
//		double sum = pair.first.get(0);
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT shotid , pow(sum - " + sum + ", 2) as dist FROM features.MotionHistogram, c WHERE shotid = c.filter ORDER BY dist ASC LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//	
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + ", q AS (SELECT sum FROM features.motionhistogram WHERE shotid = " + shotId + ") SELECT pow(q.sum - motionhistogram.sum, 2) as dist, shotid FROM features.motionhistogram, q, c WHERE shotid = c.filter ORDER BY dist ASC LIMIT " + limit);
//		return manageResultSet(rset);
//	}

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

}
