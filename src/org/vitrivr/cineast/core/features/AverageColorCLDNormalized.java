package org.vitrivr.cineast.core.features;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorLayoutDescriptor;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class AverageColorCLDNormalized extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public AverageColorCLDNormalized() {
    super("features_AverageColorCLDNormalized", 1960f / 4f, 12);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      FloatVector fv = ColorLayoutDescriptor
          .calculateCLD(ImageHistogramEqualizer.getEqualized(shot.getAvgImg()));
      persist(shot.getId(), fv);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    FloatVector query = ColorLayoutDescriptor.calculateCLD(ImageHistogramEqualizer.getEqualized(sc.getAvgImg()));
    return getSimilar(ReadableFloatVector.toArray(query), qc);
  }


}
