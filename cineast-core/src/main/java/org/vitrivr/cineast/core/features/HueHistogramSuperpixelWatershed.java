package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.List;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.HSVContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

public class HueHistogramSuperpixelWatershed extends AbstractFeatureModule {

  private final CacheConfig cacheConfig = new CacheConfig("AUTOMATIC", ".");
  private final CachedDataFactory factory = new CachedDataFactory(cacheConfig);

  public HueHistogramSuperpixelWatershed() {
    super("features_huehistogramSuperpixelWatershed", 16f, 16);
  }

  private static float[] updateHist(float[] hist, int[] colors) {
    for (int color : colors) {
      HSVContainer container = ColorConverter.RGBtoHSV(new ReadableRGBContainer(color));
      if (container.getS() > 0.2f && container.getV() > 0.3f) {
        float h = container.getH() * hist.length;
        int idx = (int) h;
        hist[idx] += h - idx;
        hist[(idx + 1) % hist.length] += idx + 1 - h;
      }
    }
    return hist;
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }

    float[] hist = new float[16];

    for (VideoFrame frame : shot.getVideoFrames()) {
      BufferedImage superpixel = applySuperpixel(frame.getImage().getBufferedImage());
      MultiImage multiImage = factory.newMultiImage(superpixel);

      updateHist(hist, multiImage.getThumbnailColors());
    }

    float sum = 0;
    for (int i = 0; i < hist.length; ++i) {
      sum += hist[i];
    }
    if (sum > 1f) {
      for (int i = 0; i < hist.length; ++i) {
        hist[i] /= sum;
      }
    }

    persist(shot.getId(), new FloatVectorImpl(hist));

  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    BufferedImage superpixel = applySuperpixel(sc);
    MultiImage multiImage = factory.newMultiImage(superpixel);

    float[] query = updateHist(new float[16], multiImage.getThumbnailColors());
    return getSimilar(query, qc);

  }

  private BufferedImage applySuperpixel(BufferedImage image) {
    return Superpixel.applySuperpixelBI(image, Superpixel.ALG_WATERSHED);
  }

  private BufferedImage applySuperpixel(SegmentContainer segmentContainer) {
    return Superpixel.applySuperpixelSC(segmentContainer, Superpixel.IMG_REP,
        Superpixel.ALG_WATERSHED);
  }


}
