package org.vitrivr.cineast.core.features;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.segmenter.FuzzyColorHistogram;
import org.vitrivr.cineast.core.extraction.segmenter.FuzzyColorHistogramCalculator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class AverageFuzzyHistNormalized extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public AverageFuzzyHistNormalized() {
    super("features_AverageFuzzyHistNormalized", 2f / 4f, 15);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(shot.getAvgImg()).getBufferedImage());
      persist(shot.getId(), fch);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized((ImageHistogramEqualizer.getEqualized(sc.getAvgImg())).getBufferedImage());
    return getSimilar(ReadableFloatVector.toArray(query), qc);
  }

  @Override
  protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return QueryConfig.clone(qc).setDistanceIfEmpty(Distance.chisquared);
  }

}
