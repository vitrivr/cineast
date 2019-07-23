package org.vitrivr.cineast.core.features;

import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorGrid8Normalized extends MedianColorGrid8 {

  public MedianColorGrid8Normalized() {
    super("features_MedianColorGrid8Normalized", 12595f / 4f);
  }


  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      MultiImage medimg = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());

      persist(shot.getId(), partition(medimg).first);
    }
  }


  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = partition(ImageHistogramEqualizer.getEqualized(sc.getMedianImg()));
    return getSimilar(ReadableFloatVector.toArray(p.first), new QueryConfig(qc).setDistanceWeights(p.second));
  }

}
