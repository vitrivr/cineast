package ch.unibas.cs.dbis.cineast.core.features.extractor;

import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;

public interface Extractor {

	void init(PersistencyWriter<?> phandler);
	
	void processShot(FrameContainer shot);
		
	void finish();
}
