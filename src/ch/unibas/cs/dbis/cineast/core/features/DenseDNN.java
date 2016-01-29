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
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;
import ch.unibas.cs.dbis.cineast.core.util.MaxPool;

@Deprecated
public class DenseDNN implements Retriever {

	private DBSelector selector;
	private static final double maxDist = 1d;
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		return new ArrayList<>(1); //TODO return some random results
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.resultsPerModule();
		ResultSet rset = this.selector.select("WITH q AS (SELECT vector FROM features.densednn WHERE id = " + shotId + ") SELECT id FROM features.densednn, q USING DISTANCE MINKOWSKI(2)(q.vector, densednn.vector) ORDER USING DISTANCE LIMIT " + limit);
		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(1);
					long id = rset.getLong(2);

					result.add(new LongDoublePair(id, MathHelper.getScore(dist, maxDist)));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return MaxPool.maxPool(result);
	}

	@Override
	public void finish() {
		this.selector.close();
	}

	@Override
	public float getConfidenceWeight() {
		// TODO Auto-generated method stub
		return 0;
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
