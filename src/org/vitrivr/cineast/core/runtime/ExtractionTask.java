package org.vitrivr.cineast.core.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;

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
    LOGGER.traceEntry();
    LOGGER.debug("Starting {} on segmentId {}", feature.getClass().getSimpleName(), shot.getId());
    long start = System.currentTimeMillis();
    try {
      feature.processSegment(shot);
    } catch (Exception e) {
      LOGGER.fatal("EXTRACTION ERROR in {}: for segmentID {} \n{}", feature.getClass().getSimpleName(), shot.getId(), LogHelper.getStackTrace(e));
    }
    long stop = System.currentTimeMillis();
    if (this.etc != null) {
      this.etc.reportExecutionTime(this.feature.getClass().getSimpleName(), (stop - start));
    }
    LOGGER.debug("Finished {} on segmentID {} in {} ms", feature.getClass().getSimpleName(), shot.getId(), (stop - start));
    LOGGER.traceExit();
  }

}
