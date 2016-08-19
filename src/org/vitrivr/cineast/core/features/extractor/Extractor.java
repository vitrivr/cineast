package org.vitrivr.cineast.core.features.extractor;

import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;

public interface Extractor {

	void init(PersistencyWriter<?> phandler);
	
	void processShot(SegmentContainer shot);
		
	void finish();
}
