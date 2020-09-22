package org.vitrivr.cineast.core.features.retriever;

import com.jogamp.common.util.ArrayHashSet;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistentOperator;

import java.util.ArrayList;
import java.util.List;

public interface Retriever extends PersistentOperator {
	void init(DBSelectorSupplier selectorSupply);

	List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc);
		
	List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc);

	default List<ScoreElement> getSimilar(List<String> segmentIds, ReadableQueryConfig qc){
		List<ScoreElement> _return = new ArrayList<>();
		for (String id : segmentIds) {
			_return.addAll(getSimilar(id, qc));
		}
		return _return;
	};

	void finish();
}
