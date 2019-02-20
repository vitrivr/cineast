package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorUtils;
import org.vitrivr.cineast.core.util.TimeHelper;

public class AverageColor extends AbstractFeatureModule {

  public AverageColor() {
    super("features_AverageColor", 196f / 4f, 3);
  }

  private static final Logger LOGGER = LogManager.getLogger();

  public static ReadableLabContainer getAvg(MultiImage img) {
    int avg = ColorUtils.getAvg(img.getColors());
    return ColorConverter.cachedRGBtoLab(avg);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    TimeHelper.tic();
    LOGGER.traceEntry();
    if (!phandler.idExists(shot.getId())) {
      if(shot.getAvgImg()==MultiImage.EMPTY_MULTIIMAGE){
        LOGGER.debug("Empty Image, skipping");
        LOGGER.traceExit();
        return;
      }
      ReadableLabContainer avg = getAvg(shot.getAvgImg());
      persist(shot.getId(), avg);
      LOGGER.debug("AverageColor.processShot() for segment {} done in {}",
          shot.getId(), TimeHelper.toc());
    }
    LOGGER.traceExit();
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    LOGGER.traceEntry();
    long start = System.currentTimeMillis();
    ReadableLabContainer query = getAvg(sc.getAvgImg());
    List<ScoreElement> _return = getSimilar(ReadableFloatVector.toArray(query), qc);
    LOGGER.debug("AverageColor.getSimilar() done in {} ms", (System.currentTimeMillis() - start));
    return LOGGER.traceExit(_return);
  }

}
