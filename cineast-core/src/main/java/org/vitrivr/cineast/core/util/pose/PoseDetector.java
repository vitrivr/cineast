package org.vitrivr.cineast.core.util.pose;

import org.vitrivr.cineast.core.data.Skeleton;

import java.awt.image.BufferedImage;
import java.util.List;

public interface PoseDetector {

    List<Skeleton> detectPoses(BufferedImage img);

}
