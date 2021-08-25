package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentOperator;

import java.util.function.Supplier;

public interface Extractor extends PersistentOperator {
	void init(Supplier<PersistencyWriter<?>> phandlerSupply, int batchSize);

	void processSegment(SegmentContainer shot);
		
	void finish();
}
