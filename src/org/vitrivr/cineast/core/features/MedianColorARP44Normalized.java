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
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ARPartioner;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class MedianColorARP44Normalized extends AbstractFeatureModule {

  public MedianColorARP44Normalized() {
    super("features_MedianColorARP44Normalized", 115854f / 4f, 96);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      MultiImage median = ImageHistogramEqualizer.getEqualized(shot.getMedianImg());
      FloatVector vec = ARPartioner.partitionImage(median, 4, 4).first;
      persist(shot.getId(), vec);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = ARPartioner.partitionImage(ImageHistogramEqualizer.getEqualized(sc.getMedianImg()), 4, 4);
    return getSimilar(ReadableFloatVector.toArray(p.first), new QueryConfig(qc).setDistanceWeights(p.second));
  }

}
