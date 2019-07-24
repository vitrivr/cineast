package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.MotionHistogramCalculator;

import java.util.ArrayList;
import java.util.List;

public class SubDivMotionSum4 extends MotionHistogramCalculator {


  public SubDivMotionSum4() {
    super("features_SubDivMotionHistogram4", "sums", 100 * 16, 4);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(4, sc.getPaths());

    FloatVectorImpl fv = new FloatVectorImpl(pair.first);
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
  }

}
