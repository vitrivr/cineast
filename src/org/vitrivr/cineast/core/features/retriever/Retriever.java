package org.vitrivr.cineast.core.features.retriever;

import java.util.List;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistentOperator;

public interface Retriever extends PersistentOperator {

	void init(DBSelectorSupplier selectorSupply);

	List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc);
		
	List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc);

	void finish();
	
}
