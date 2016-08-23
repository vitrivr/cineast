package org.vitrivr.cineast.core.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.DecodingError;

class ExtractionTask implements Runnable {

	private Extractor feature;
	private SegmentContainer shot;
	private static final Logger LOGGER = LogManager.getLogger();
	
	ExtractionTask(Extractor feature, SegmentContainer shot) {
		this.feature = feature;
		this.shot = shot;
	}
	
	@Override
	public void run() {
		LOGGER.entry();
		LOGGER.debug("starting {} on shotId {}", feature.getClass().getSimpleName(), shot.getId());
		try{
			feature.processShot(shot);
		}catch(DecodingError e){
			LOGGER.fatal("DECODING ERROR");
			throw e;
		}
		LOGGER.exit();
	}

}
