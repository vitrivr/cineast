package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

@Deprecated
public class RelativePosition implements Retriever {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private DBSelector selector;

	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {

		int limit = Config.resultsPerModule() * 10;
		
		ResultSet rset = selector.select("SELECT shots.id, pow((cast(shots.startframe as float) / cast(videos.frames as float)) - " + qc.getRelativeStart() + ", 2) + pow((cast(shots.endframe as float) / cast(videos.frames as float)) - " + qc.getRelativeEnd() + ", 2) as distance FROM cineast.shots join cineast.videos on (shots.video = videos.id) ORDER BY 2 ASC LIMIT " + limit);
		
		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(2);
					long shotId = rset.getLong(1);

					dist = 1 - (dist / 2d);
					
					result.add(new LongDoublePair(shotId, dist));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return result;
	}

	@Override
	public void finish() {
		if(this.selector != null){
			this.selector.close();
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.resultsPerModule() * 10;
		
		ResultSet rset = selector.select("WITH q AS (SELECT (cast(shots.startframe as float) / cast(videos.frames as float)) AS start, (cast(shots.endframe as float) / cast(videos.frames as float)) AS end FROM cineast.shots join cineast.videos on (shots.video = videos.id) WHERE shots.id = " + shotId + ") " + 
										"SELECT shots.id, pow((cast(shots.startframe as float) / cast(videos.frames as float)) - q.start, 2) + pow((cast(shots.endframe as float) / cast(videos.frames as float)) - q.end, 2) as distance FROM cineast.shots, q, cineast.videos WHERE (shots.video = videos.id) ORDER BY 2 ASC LIMIT " + limit);
		
		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(2);
					long id = rset.getLong(1);

					dist = 1 - (dist / 2d);
					
					result.add(new LongDoublePair(id, dist));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return result;
	}

	@Override
	public float getConfidenceWeight() {
		// TODO 
		return 1f;
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		return new ArrayList<>(1);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		return new ArrayList<>(1);
	}
	
}
