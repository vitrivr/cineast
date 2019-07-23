package org.vitrivr.cineast.core.features;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

public class AverageColorGrid8Reduced11 extends AverageColorGrid8 {

  private static final Logger LOGGER = LogManager.getLogger();

  public AverageColorGrid8Reduced11() {
    super("features_AverageColorGrid8Reduced11", 12595f / 4f);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      MultiImage avgimg = ColorReductionUtil.quantize11(shot.getAvgImg());

      persist(shot.getId(), partition(avgimg).first);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = partition(ColorReductionUtil.quantize11(sc.getAvgImg()));
    return getSimilar(ReadableFloatVector.toArray(p.first), new QueryConfig(qc).setDistanceWeights(p.second));
  }

}
