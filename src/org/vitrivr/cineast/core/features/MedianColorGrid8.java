package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.ColorUtils;
import org.vitrivr.cineast.core.util.GridPartitioner;

public class MedianColorGrid8 extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public MedianColorGrid8() {
    super("features_MedianColorGrid8", 12595f / 4f, 192);
  }

  protected MedianColorGrid8(String tableName, float maxDist) {
    super(tableName, maxDist, 192);
  }


  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      MultiImage medimg = shot.getMedianImg();

      persist(shot.getId(), partition(medimg).first);
    }
  }

  protected static Pair<FloatVector, float[]> partition(MultiImage img) {
    ArrayList<Integer> labs = new ArrayList<Integer>(img.getWidth() * img.getHeight());
    ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
    int[] colors = img.getColors();
    for (int c : colors) {
      labs.add(c);
      alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
    }

    ArrayList<LinkedList<Integer>> partitions = GridPartitioner
        .partition(labs, img.getWidth(), img.getHeight(), 8, 8);

    float[] result = new float[8 * 8 * 3];
    int i = 0;
    for (LinkedList<Integer> list : partitions) {
      ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(ColorUtils.median(list));
      result[i++] = lab.getL();
      result[i++] = lab.getA();
      result[i++] = lab.getB();
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
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = partition(sc.getMedianImg());
    return getSimilar(ReadableFloatVector.toArray(p.first), new QueryConfig(qc).setDistanceWeights(p.second));
  }

}
