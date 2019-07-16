package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.LabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.providers.MedianImgProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.TimeHelper;

public class MedianColor extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public MedianColor() {
    super("features_MedianColor", 196f / 4f, 3);
  }

  protected LabContainer getMedian(MedianImgProvider provider) {
    return MedianColor.getMedian(provider.getMedianImg());
  }

  public static LabContainer getMedian(MultiImage img) {
    int[] r = new int[256], g = new int[256], b = new int[256];
    int[] colors = img.getColors();

    for (int color : colors) {
      if (ReadableRGBContainer.getAlpha(color) < 127) {
        continue;
      }
      r[ReadableRGBContainer.getRed(color)]++;
      g[ReadableRGBContainer.getGreen(color)]++;
      b[ReadableRGBContainer.getBlue(color)]++;
    }

    return ColorConverter
        .RGBtoLab(medianFromHistogram(r), medianFromHistogram(g), medianFromHistogram(b));
  }

  private static int medianFromHistogram(int[] hist) {
    int pos_l = 0, pos_r = hist.length - 1;
    int sum_l = hist[pos_l], sum_r = hist[pos_r];

    while (pos_l < pos_r) {
      if (sum_l < sum_r) {
        sum_l += hist[++pos_l];
      } else {
        sum_r += hist[--pos_r];
      }
    }
    return pos_l;
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      LabContainer median = getMedian(shot);

      persist(shot.getId(), median);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    LabContainer query = getMedian(sc.getMedianImg());
    return getSimilar(ReadableFloatVector.toArray(query), qc);
  }

}
