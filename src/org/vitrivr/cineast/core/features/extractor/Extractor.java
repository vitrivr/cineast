package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

public interface Extractor {

	void init(PersistencyWriterSupplier phandlerSupply);
	
	void processShot(SegmentContainer shot);
		
	void finish();
}
