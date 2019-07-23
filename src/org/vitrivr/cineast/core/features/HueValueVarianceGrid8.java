package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.HSVContainer;
import org.vitrivr.cineast.core.color.RGBContainer;
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
import org.vitrivr.cineast.core.util.GridPartitioner;

public class HueValueVarianceGrid8 extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public HueValueVarianceGrid8() {
    super("features_HueValueVarianceGrid8", 17f / 4f, 128);
  }

  private Pair<FloatVector, float[]> computeGrid(SegmentContainer qc) {
    ArrayList<SummaryStatistics> stats = new ArrayList<SummaryStatistics>(64);
    for (int i = 0; i < 128; ++i) {
      stats.add(new SummaryStatistics());
    }
    MultiImage img = qc.getMostRepresentativeFrame().getImage();
    int[] colors = img.getColors();
    ArrayList<HSVContainer> hsvs = new ArrayList<HSVContainer>(img.getWidth() * img.getHeight());
    ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
    for (int c : colors) {
      hsvs.add(ColorConverter.RGBtoHSV(new RGBContainer(c)));
      alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
    }

    ArrayList<LinkedList<HSVContainer>> partitions = GridPartitioner.partition(hsvs, img.getWidth(),
        img.getHeight(), 8, 8);
    ArrayList<LinkedList<Float>> alphaPartitions = GridPartitioner.partition(alphas, img.getWidth(),
        img.getHeight(), 8, 8);
    for (int i = 0; i < partitions.size(); ++i) {
      SummaryStatistics hue = stats.get(2 * i);
      SummaryStatistics value = stats.get(2 * i + 1);
      Iterator<Float> iter = alphaPartitions.get(i).iterator();
      for (HSVContainer c : partitions.get(i)) {
        if (iter.next() < 0.5f) {
          continue;
        }
        hue.addValue(c.getH());
        value.addValue(c.getV());
      }
    }
    float[] f = new float[128];
    for (int i = 0; i < 128; ++i) {
      f[i] = (float) stats.get(i).getVariance();
    }

    float[] weights = new float[128];
    for (int i = 0; i < alphaPartitions.size(); ++i) {
      float w = 0;
      for (float v : alphaPartitions.get(i)) {
        w += v;
      }
      w /= alphaPartitions.get(i).size();
      weights[2 * i] = w;
      weights[2 * i + 1] = w;
    }

    return new Pair<FloatVector, float[]>(new FloatVectorImpl(f), weights);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      SummaryStatistics[] stats = new SummaryStatistics[128];
      for (int i = 0; i < 128; ++i) {
        stats[i] = new SummaryStatistics();
      }
      ArrayList<HSVContainer> hsvs = null;
      List<VideoFrame> videoFrames = shot.getVideoFrames();
      for (VideoFrame f : videoFrames) {
        MultiImage img = f.getImage();
        int[] ints = img.getColors();

        if (hsvs == null) {
          hsvs = new ArrayList<HSVContainer>(ints.length);
        } else {
          hsvs.clear();
        }

        for (int c : ints) {
          hsvs.add(ColorConverter.RGBtoHSV(new RGBContainer(c)));
        }

        ArrayList<LinkedList<HSVContainer>> partition = GridPartitioner.partition(hsvs,
            img.getWidth(), img.getHeight(), 8, 8);
        for (int i = 0; i < 64; ++i) {
          LinkedList<HSVContainer> list = partition.get(i);
          SummaryStatistics hue = stats[2 * i];
          SummaryStatistics value = stats[2 * i + 1];
          for (HSVContainer c : list) {
            hue.addValue(c.getH());
            value.addValue(c.getV());
          }
        }
      }

      float[] fv = new float[128];
      int i = 0;
      for (SummaryStatistics s : stats) {
        fv[i++] = (float) s.getVariance();
      }

      persist(shot.getId(), new FloatVectorImpl(fv));
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<FloatVector, float[]> p = computeGrid(sc);
    return getSimilar(ReadableFloatVector.toArray(p.first), new QueryConfig(qc).setDistanceWeights(p.second));
  }

}
