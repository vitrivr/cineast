package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;
import org.vitrivr.cineast.core.util.CineastConstants;

public class ExternalTextFeature extends AbstractTextRetriever {

  public static final String DEFAULT_TABLE_NAME = "features_externalText";

  final FeatureClient client;

  public ExternalTextFeature() {
    super(DEFAULT_TABLE_NAME);
    throw new IllegalArgumentException("no properties specified");
  }

  public ExternalTextFeature(Map<String, String> properties) throws IOException {
    super(properties.getOrDefault(CineastConstants.ENTITY_NAME_KEY, DEFAULT_TABLE_NAME), properties);

    this.client = FeatureClient.build(properties);
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public void processSegment(SegmentContainer sc) {
    if (sc.getMostRepresentativeFrame() == null || sc.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    try {
      Map<String,Object> extractions = client.extract(sc);
      String feature = (String) extractions.getOrDefault("raw_text", "");
      var descriptor = new SimpleFulltextFeatureDescriptor(sc.getId(), feature);
      this.writer.write(descriptor);
    } catch (Exception e) {
      LOGGER.error("Error during extraction", e);
    }

  }
}
