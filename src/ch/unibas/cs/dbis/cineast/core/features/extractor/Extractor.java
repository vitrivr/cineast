package ch.unibas.cs.dbis.cineast.core.features.extractor;

import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;

public interface Extractor {

	void init(PersistencyWriter<?> phandler);
	
	void processShot(SegmentContainer shot);
		
	void finish();
}
