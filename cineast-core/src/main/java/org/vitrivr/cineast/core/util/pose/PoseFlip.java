package org.vitrivr.cineast.core.util.pose;


import org.apache.commons.lang3.SerializationUtils;

public class PoseFlip {
  public static float[][][] flip(float[][][] poses) {
    float[][][] result = SerializationUtils.clone(poses);
    for (float[][] pose: result) {
      // Step 1. x-flip all keypoints
      for (float[] point : pose) {
        point[0] = -point[0];
      }
      // Step 2. swap left and right keypoint pairs
      int[][] kpPairs = PoseSpecs.getInstance().kpPairs;
      for (int[] pair : kpPairs) {
        float[] tmp = pose[pair[0]];
        pose[pair[0]] = pose[pair[1]];
        pose[pair[1]] = tmp;
      }
    }
    return result;
  }

  public static String modelFlip(String modelName) {
    return PoseSpecs.getInstance().flips.getOrDefault(modelName, modelName);
  }
}
