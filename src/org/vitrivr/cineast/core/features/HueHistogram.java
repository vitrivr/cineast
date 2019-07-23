package org.vitrivr.cineast.core.features;

import java.util.List;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.HSVContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

public class HueHistogram extends AbstractFeatureModule {

  public HueHistogram() {
    super("features_huehistogram", 16f, 16);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }

    float[] hist = new float[16];

    for (VideoFrame frame : shot.getVideoFrames()){
      updateHist(hist, frame.getImage().getThumbnailColors());
    }

    float sum = 0;
    for(int i = 0; i < hist.length; ++i){
      sum += hist[i];
    }
    if(sum > 1f){
      for(int i = 0; i < hist.length; ++i){
        hist[i] /= sum;
      }
    }

    persist(shot.getId(), new FloatVectorImpl(hist));

  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    float[] query = updateHist(new float[16], sc.getMostRepresentativeFrame().getImage().getThumbnailColors());
    return getSimilar(query, qc);

  }

  private static float[] updateHist(float[] hist, int[] colors){
    for(int color : colors){
      HSVContainer container = ColorConverter.RGBtoHSV(new ReadableRGBContainer(color));
      if(container.getS() > 0.2f && container.getV() > 0.3f){
        float h = container.getH() * hist.length;
        int idx = (int) h;
        hist[idx] += h - idx;
        hist[(idx + 1) % hist.length] += idx + 1 - h;
      }
    }
    return hist;
  }

}
