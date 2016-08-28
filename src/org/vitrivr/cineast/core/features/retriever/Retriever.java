package org.vitrivr.cineast.core.features.retriever;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

public interface Retriever {

	void init(DBSelectorSupplier selectorSupply);

	List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc);
		
	List<StringDoublePair> getSimilar(String shotId, QueryConfig qc);

	void finish();
	
}
