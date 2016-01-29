package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import georegression.struct.point.Point2D_F32;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public abstract class MotionHistogramCalculator implements Retriever {

	protected DBSelector selector;
	private final float maxDist;
	private final String colName;
	protected final String tableName;
	private static Logger LOGGER = LogManager.getLogger();


	protected MotionHistogramCalculator(String tableName, String colName, float maxDist){
		this.maxDist = maxDist;
		this.tableName = tableName;
		this.colName = colName;
	}
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	private static int getidx(int subdiv, float x, float y) {
		int ix = (int) Math.floor(subdiv * x), iy = (int) Math.floor(subdiv * y);
		ix = Math.max(Math.min(ix, subdiv - 1), 0);
		iy = Math.max(Math.min(iy, subdiv - 1), 0);

		return ix * subdiv + iy;
	}

	protected Pair<List<Double>, ArrayList<ArrayList<Float>>> getSubDivHist(
			int subdiv, List<LinkedList<Point2D_F32>> paths) {

		double[] sums = new double[subdiv * subdiv];
		float[][] hists = new float[subdiv * subdiv][8];

		for (LinkedList<Point2D_F32> path : paths) {
			if (path.size() > 1) {
				Iterator<Point2D_F32> iter = path.iterator();
				Point2D_F32 last = iter.next();
				while (iter.hasNext()) {
					Point2D_F32 current = iter.next();
					double dx = current.x - last.x, dy = current.y - last.y;
					int idx = ((int) Math.floor(4 * Math.atan2(dy, dx)
							/ Math.PI) + 4) % 8;
					double len = Math.sqrt(dx * dx + dy * dy);
					hists[getidx(subdiv, last.x, last.y)][idx] += len;
					last = current;
				}
			}
		}

		for (int i = 0; i < sums.length; ++i) {
			float[] hist = hists[i];
			double sum = 0;
			for (int j = 0; j < hist.length; ++j) {
				sum += hist[j];
			}
			if (sum > 0) {
				for (int j = 0; j < hist.length; ++j) {
					hist[j] /= sum;
				}
				hists[i] = hist;
			}
			sums[i] = sum;
		}

		ArrayList<Double> sumList = new ArrayList<Double>(sums.length);
		for (double d : sums) {
			sumList.add(d);
		}

		ArrayList<ArrayList<Float>> histList = new ArrayList<ArrayList<Float>>(
				hists.length);
		for (float[] hist : hists) {
			ArrayList<Float> h = new ArrayList<Float>(8);
			for (float f : hist) {
				h.add(f);
			}
			histList.add(h);
		}

		return new Pair<List<Double>, ArrayList<ArrayList<Float>>>(sumList,
				histList);
	}
	
	@Override
	public void finish(){
		if(this.selector != null){
			this.selector.close();
		}
	}

	protected ArrayList<LongDoublePair> manageResultSet(ResultSet rset){
		ArrayList<LongDoublePair> result = new ArrayList<>(Config.resultsPerModule());
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(1);
					long shotId = rset.getLong(2);

					result.add(new LongDoublePair(shotId, MathHelper.getScore(dist, this.maxDist)));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return result;
	}
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.resultsPerModule();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("WITH q AS (SELECT ");
		sb.append(colName);
		sb.append(" FROM ");
		sb.append(this.tableName);
		sb.append(" WHERE shotid = ");
		sb.append(shotId);
		sb.append(") SELECT shotid FROM ");
		sb.append(this.tableName);
		sb.append(", q USING DISTANCE MINKOWSKI(2)");
		sb.append("(q.");
		sb.append(colName);
		sb.append(", ");
		sb.append(this.tableName);
		sb.append('.');
		sb.append(colName);
		sb.append(") ORDER USING DISTANCE LIMIT ");
		sb.append(limit);
		
		ResultSet rset = this.selector.select(sb.toString());
		return manageResultSet(rset);

	}
	
	
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		int limit = Config.resultsPerModule();

		StringBuilder sb = new StringBuilder();
		
		sb.append("WITH q AS (SELECT ");
		sb.append(colName);
		sb.append(" FROM ");
		sb.append(tableName);
		sb.append(" WHERE shotid = ");
		sb.append(shotId);
		sb.append("), c AS (SELECT shotid AS filter FROM cineast.resultcacheelements, cineast.resultcachenames");
		sb.append(" WHERE resultcacheelements.chacheid = resultcachenames.id AND resultcachenames.name = '");
		sb.append(resultCacheName);
		sb.append("') SELECT shotid FROM ");
		sb.append(tableName);
		sb.append(", q, c");
		sb.append(" WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)");
		sb.append("(q.");
		sb.append(colName);
		sb.append(", ");
		sb.append(tableName);
		sb.append('.');
		sb.append(colName);
		sb.append(") ORDER USING DISTANCE LIMIT ");
		sb.append(limit);
		
		ResultSet rset = this.selector.select(sb.toString());
		return manageResultSet(rset);
	}
	
	protected String getResultCacheLimitSQL(String resultCacheName){
		StringBuilder sb = new StringBuilder();
		sb.append("WITH c AS (SELECT shotid AS filter FROM cineast.resultcacheelements, cineast.resultcachenames WHERE ");
		sb.append("resultcacheelements.chacheid = resultcachenames.id AND resultcachenames.name = '");
		sb.append(resultCacheName);
		sb.append("')");
		return sb.toString();
	}
	
	@Override
	public float getConfidenceWeight() {
		// TODO 
		return 1f;
	}

}
