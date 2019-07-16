package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

public class STMP7EH extends EHD {

  private static final Logger LOGGER = LogManager.getLogger();

  public STMP7EH(){
    super(160);
  }

  @Override
  public void init(PersistencyWriterSupplier supply) {
    this.phandler = supply.get();
    this.phandler.open("features_STMP7EH");
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      List<VideoFrame> videoFrames = shot.getVideoFrames();
      float[] hist = new float[80];
      SummaryStatistics[] stats = new SummaryStatistics[80];
      for (int i = 0; i < 80; ++i) {
        stats[i] = new SummaryStatistics();
      }
      for (VideoFrame f : videoFrames) {
        MultiImage img = f.getImage();

        hist = process(img, hist);
        for (int i = 0; i < 80; ++i) {
          stats[i].addValue(hist[i]);
          hist[i] = 0f;
        }
      }
      float[] vec = new float[160];
      for (int i = 0; i < 80; ++i) {
        vec[i] = (float) stats[i].getMean();
        vec[i + 80] = (float) Math.sqrt(stats[i].getVariance());
      }
      persist(shot.getId(), new FloatVectorImpl(vec));
    }
  }

}
