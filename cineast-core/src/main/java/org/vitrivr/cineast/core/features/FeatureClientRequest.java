package org.vitrivr.cineast.core.features;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.web.WebClient;

abstract class FeatureClientRequest {

  protected static final Logger LOGGER = LogManager.getLogger();

  abstract public String execute(SegmentContainer sc, WebClient client) throws IOException, InterruptedException;
}
