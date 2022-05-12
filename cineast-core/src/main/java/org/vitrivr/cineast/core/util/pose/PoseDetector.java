package org.vitrivr.cineast.core.util.pose;

import java.awt.image.BufferedImage;
import java.util.List;
import org.vitrivr.cineast.core.data.Skeleton;

public interface PoseDetector {

  List<Skeleton> detectPoses(BufferedImage img);

}
