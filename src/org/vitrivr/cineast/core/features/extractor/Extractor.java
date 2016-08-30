package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentOperator;

public interface Extractor extends PersistentOperator {

	void init(PersistencyWriterSupplier phandlerSupply);
	
	void processShot(SegmentContainer shot);
		
	void finish();
}
