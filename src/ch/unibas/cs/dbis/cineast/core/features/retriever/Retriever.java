package ch.unibas.cs.dbis.cineast.core.features.retriever;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;

public interface Retriever {

	void init(DBSelector selector);
	
	List<LongDoublePair> getSimilar(FrameContainer qc);

	List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName);
	
	List<LongDoublePair> getSimilar(long shotId);
	
	List<LongDoublePair> getSimilar(long shotId, String resultCacheName);

	void finish();
	
	/**
	 * returns a per-query confidence describing how much weight the results should be given
	 * @return a value between 0 and 1
	 */
	float getConfidenceWeight();
}
