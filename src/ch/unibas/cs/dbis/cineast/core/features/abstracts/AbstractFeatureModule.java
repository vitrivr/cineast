package ch.unibas.cs.dbis.cineast.core.features.abstracts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FeatureString;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public abstract class AbstractFeatureModule implements Extractor, Retriever {

	private static Logger LOGGER = LogManager.getLogger();
	
	protected PersistencyWriter phandler;
	protected DBSelector selector;
	protected final float maxDist;
	protected final String colName, tableName;
	private float confidence = 1f;

	protected AbstractFeatureModule(String tableName, String colName, float maxDist){
		this.colName = colName;
		this.tableName = tableName;
		this.maxDist = maxDist;
	}
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open(this.tableName);
	}
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	protected void addToDB(long shotId, FeatureString fs) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, fs);
		this.phandler.write(tuple);
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
		sb.append(", q USING DISTANCE MINKOWSKI(1)");
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

	@Override
	public void finish() {
		if (this.phandler != null) {
			this.phandler.close();
		}
		if (this.selector != null) {
			this.selector.close();
		}
	}

	protected static final String formatQueryWeights(float[] weights){
		StringBuilder sb = new StringBuilder("'{");
		for(int i = 0; i < weights.length - 1; ++i){
			sb.append(weights[i]);
			sb.append(", ");
		}
		sb.append(weights[weights.length - 1]);
		sb.append("}'");
		return sb.toString();
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
	public float getConfidenceWeight() {
		return this.confidence;
	}
	
	protected void setConfidenceWeight(float weight){
		this.confidence = Math.min(1f, Math.max(0f, weight));
	}
	
	protected String getResultCacheLimitSQL(String resultCacheName){
		StringBuilder sb = new StringBuilder();
		sb.append("WITH c AS (SELECT shotid AS filter FROM cineast.resultcacheelements, cineast.resultcachenames WHERE ");
		sb.append("resultcacheelements.chacheid = resultcachenames.id AND resultcachenames.name = '");
		sb.append(resultCacheName);
		sb.append("')");
		return sb.toString();
	}
}
