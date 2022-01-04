package org.vitrivr.cineast.core.features;

import java.util.List;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorLayoutDescriptor;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

public class CLDReduced11 extends AbstractFeatureModule {

  public CLDReduced11() {
    super("features_CLDReduced11", 1960f / 4f, 12);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      FloatVector fv = ColorLayoutDescriptor.calculateCLD(ColorReductionUtil.quantize11(shot.getMostRepresentativeFrame().getImage()));
      persist(shot.getId(), fv);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    FloatVector query = ColorLayoutDescriptor.calculateCLD(ColorReductionUtil.quantize11(sc.getMostRepresentativeFrame().getImage()));
    return getSimilar(ReadableFloatVector.toArray(query), qc);
  }


}
