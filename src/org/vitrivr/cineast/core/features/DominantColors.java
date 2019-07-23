package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.LabContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.KMeansPP;
import org.vitrivr.cineast.core.util.TimeHelper;

public class DominantColors extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public DominantColors() {
    super("features_DominantColors", 488f / 4f, 9);
  }

  public static LabContainer[] getDominantColor(MultiImage img) {
    int[] colors = img.getThumbnailColors();
    ArrayList<ReadableLabContainer> colorList = new ArrayList<ReadableLabContainer>(colors.length);
    for (int col : colors) {
      if (ReadableRGBContainer.getAlpha(col) < 127) {
        continue;
      }
      colorList.add(ColorConverter.cachedRGBtoLab(col));
    }

    if (colorList.size() < 3) {
      return new LabContainer[]{new LabContainer(), new LabContainer(), new LabContainer()};
    }

    KMeansPP.KMenasResult<ReadableLabContainer> result = KMeansPP
        .bestOfkMeansPP(colorList, new LabContainer(0, 0, 0), 3, 0.001, 10);

    FloatVector[] vectors = result.getCenters().toArray(new FloatVectorImpl[3]);
    LabContainer[] _return = new LabContainer[]{new LabContainer(0, 0, 0),
        new LabContainer(0, 0, 0), new LabContainer(0, 0, 0)};
    for (int i = 0; i < Math.min(3, vectors.length); ++i) {
      if (vectors[i] == null) {
        break;
      }
      _return[i] = new LabContainer(vectors[i].getElement(0), vectors[i].getElement(1),
          vectors[i].getElement(2));
    }
    return _return;
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      LabContainer[] dominant = getDominantColor(shot.getMostRepresentativeFrame().getImage());
      persist(shot.getId(), dominant);
    }
  }

  private void persist(String shotId, LabContainer[] dominant) {
    FloatVectorImpl fvi = new FloatVectorImpl();
    for (LabContainer lab : dominant) {
      fvi.add(lab.getL());
      fvi.add(lab.getA());
      fvi.add(lab.getB());
    }
    super.persist(shotId, fvi);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    LabContainer[] query = getDominantColor(sc.getMostRepresentativeFrame().getImage());
    FloatVectorImpl fvi = new FloatVectorImpl();
    for (LabContainer lab : query) {
      fvi.add(lab.getL());
      fvi.add(lab.getA());
      fvi.add(lab.getB());
    }
    return getSimilar(ReadableFloatVector.toArray(fvi), qc);
  }


}
