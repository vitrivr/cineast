package ch.unibas.cs.dbis.cineast.core.features.retriever;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;

public interface Retriever {

	void init(DBSelector selector);

	List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc);
		
	List<StringDoublePair> getSimilar(long shotId, QueryConfig qc);

	void finish();
	
}
