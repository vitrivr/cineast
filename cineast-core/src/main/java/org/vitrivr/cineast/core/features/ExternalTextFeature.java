package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

public class ExternalTextFeature extends AbstractTextRetriever {

  final AbstractFeatureClientWrapper client;
  public ExternalTextFeature(Map<String, String> properties) throws Exception {
    super("externalText", properties); // get or default tablenamekey (oder so)
    this.client = AbstractFeatureClientWrapper.build(properties);
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public void processSegment(SegmentContainer sc){
    BufferedImage img = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
    try {
      String feature = client.extractTextFromImage(img);
      var descriptor = new SimpleFulltextFeatureDescriptor(sc.getId(), feature);
      this.writer.write(descriptor);
    } catch (Exception e) {
      LOGGER.error("Error during extraction", e);
    }

  }
}
