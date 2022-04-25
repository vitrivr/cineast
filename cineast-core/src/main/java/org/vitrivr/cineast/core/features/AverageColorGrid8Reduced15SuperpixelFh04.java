package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.util.ColorReductionUtil;

public class AverageColorGrid8Reduced15SuperpixelFh04 extends AverageColorGrid8 {

  private static final Logger LOGGER = LogManager.getLogger();

  private final CacheConfig cacheConfig = new CacheConfig("AUTOMATIC", ".");
  private final CachedDataFactory factory = new CachedDataFactory(cacheConfig);

  public AverageColorGrid8Reduced15SuperpixelFh04() {
    super("features_AverageColorGrid8Reduced15SuperpixelFh04", 12595f / 4f);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      BufferedImage superpixel = applySuperpixel(shot);

      MultiImage multiImage = factory.newMultiImage(superpixel);
      MultiImage avgimg = ColorReductionUtil.quantize15(multiImage);
      persist(shot.getId(), partition(avgimg).first);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    BufferedImage superpixel = applySuperpixel(sc);

    MultiImage multiImage = factory.newMultiImage(superpixel);
    Pair<FloatVector, float[]> p = partition(ColorReductionUtil.quantize15(multiImage));
    return getSimilar(ReadableFloatVector.toArray(p.first),
        new QueryConfig(qc).setDistanceWeights(p.second));
  }

  private BufferedImage applySuperpixel(SegmentContainer segmentContainer) {
    return Superpixel.applySuperpixelSC(segmentContainer, Superpixel.IMG_AVG, Superpixel.ALG_FH04);
  }

}
