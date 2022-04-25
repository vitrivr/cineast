package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorUtils;
import org.vitrivr.cineast.core.util.GridPartitioner;

public class AverageColorGrid8SuperpixelMeanShift extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();
  private final CacheConfig cacheConfig = new CacheConfig("AUTOMATIC", ".");
  private final CachedDataFactory factory = new CachedDataFactory(cacheConfig);

  public AverageColorGrid8SuperpixelMeanShift() {
    super("features_AverageColorGrid8SuperpixelMeanShift", 12595f / 4f, 192);
  }

  protected AverageColorGrid8SuperpixelMeanShift(String tableName, float maxDist) {
    super(tableName, maxDist, 192);
  }

  protected static Pair<FloatVector, float[]> partition(MultiImage img) {
    ArrayList<ReadableLabContainer> labs = new ArrayList<ReadableLabContainer>(
        img.getWidth() * img.getHeight());
    ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
    int[] colors = img.getColors();
    for (int c : colors) {
      labs.add(ColorConverter.cachedRGBtoLab(c));
      alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
    }

    ArrayList<LinkedList<ReadableLabContainer>> partitions = GridPartitioner
        .partition(labs, img.getWidth(), img.getHeight(), 8, 8);

    float[] result = new float[8 * 8 * 3];
    int i = 0;
    for (LinkedList<ReadableLabContainer> list : partitions) {
      ReadableLabContainer avg = ColorUtils.getAvg(list);
      result[i++] = avg.getL();
      result[i++] = avg.getA();
      result[i++] = avg.getB();
    }

    ArrayList<LinkedList<Float>> alphaPartitions = GridPartitioner
        .partition(alphas, img.getWidth(), img.getHeight(), 8, 8);
    float[] weights = new float[8 * 8 * 3];
    i = 0;
    for (LinkedList<Float> list : alphaPartitions) {
      float a = 0;
      int c = 0;
      for (float f : list) {
        a += f;
        ++c;
      }
      weights[i++] = a / c;
      weights[i++] = a / c;
      weights[i++] = a / c;
    }

    return new Pair<FloatVector, float[]>(new FloatVectorImpl(result), weights);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getAvgImg() == MultiImage.EMPTY_MULTIIMAGE) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      //MultiImage avgimg = shot.getAvgImg();
      BufferedImage superpixel = applySuperpixel(shot);

      MultiImage multiImage = factory.newMultiImage(superpixel);
      persist(shot.getId(), partition(multiImage).first);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    BufferedImage superpixel = applySuperpixel(sc);

    MultiImage multiImage = factory.newMultiImage(superpixel);
    Pair<FloatVector, float[]> p = partition(multiImage);
    return getSimilar(ReadableFloatVector.toArray(p.first),
        new QueryConfig(qc).setDistanceWeights(p.second));
  }

  private BufferedImage applySuperpixel(SegmentContainer segmentContainer) {
    return Superpixel.applySuperpixelSC(segmentContainer, Superpixel.IMG_AVG, Superpixel.ALG_MS);
  }


}
