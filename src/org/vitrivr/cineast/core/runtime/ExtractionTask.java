package org.vitrivr.cineast.core.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.DecodingError;

class ExtractionTask implements Runnable {

	private final Extractor feature;
	private final SegmentContainer shot;
	private final ExecutionTimeCounter etc;
	private static final Logger LOGGER = LogManager.getLogger();
	
	ExtractionTask(Extractor feature, SegmentContainer shot, ExecutionTimeCounter etc) {
		this.feature = feature;
		this.shot = shot;
		this.etc = etc;
	}
	
	@Override
	public void run() {
		LOGGER.entry();
		LOGGER.debug("Starting {} on segmentId {}", feature.getClass().getSimpleName(), shot.getId());
		try{
		  long start = System.currentTimeMillis();
			feature.processShot(shot);
			if(this.etc != null){
			  this.etc.reportExecutionTime(this.feature.getClass(), (System.currentTimeMillis() - start));
			}
		}catch(DecodingError e){
			LOGGER.fatal("DECODING ERROR");
			throw e;
		}
		LOGGER.exit();
	}

}
