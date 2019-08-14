package org.vitrivr.cineast.core.features.retriever;

import java.util.Optional;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistentOperator;

import java.util.List;

public interface Retriever extends PersistentOperator {

	void init(DBSelectorSupplier selectorSupply);

	List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc);
		
	List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc);

	void finish();

	/**
	 * @return The tables which this retriever uses to store its data.
	 */
	List<String> getTableNames();
	
}
