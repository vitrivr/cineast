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

public class SubDivMotionSum5 extends MotionHistogramCalculator {


  public SubDivMotionSum5() {
    super("features_SubDivMotionHistogram5", "sums", 2500, 5);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(5, sc.getBgPaths());
    FloatVectorImpl fv = new FloatVectorImpl(pair.first);
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
  }

}
