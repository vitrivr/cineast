package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.MotionHistogramCalculator;

public class MotionSumBackground extends MotionHistogramCalculator {

	public MotionSumBackground() {
		super("features.MotionHistogramBackground", "sum", 1);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getBgPaths());

		double sum = pair.first.get(0);
		
		ResultSet rset = this.selector.select("SELECT shotid , pow(sum - " + sum + ", 2) as dist FROM features.MotionHistogramBackground ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("WITH q AS (SELECT sum FROM features.MotionHistogramBackground WHERE shotid = " + shotId + ") SELECT pow(q.sum - MotionHistogramBackground.sum, 2) as dist, shotid FROM features.MotionHistogramBackground, q  ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getBgPaths());

		double sum = pair.first.get(0);
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT shotid , pow(sum - " + sum + ", 2) as dist FROM features.MotionHistogramBackground, c WHERE shotid = c.filter ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + ", q AS (SELECT sum FROM features.MotionHistogramBackground WHERE shotid = " + shotId + ") SELECT pow(q.sum - MotionHistogramBackground.sum, 2) as dist, shotid FROM features.MotionHistogramBackground, q, c WHERE shotid = c.filter ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

}
