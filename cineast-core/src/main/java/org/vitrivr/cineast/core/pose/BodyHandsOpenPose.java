package org.vitrivr.cineast.core.pose;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.openpose.Datum;
import org.bytedeco.openpose.Datums;
import org.bytedeco.openpose.FloatArray;
import org.bytedeco.openpose.FloatArray2;
import org.bytedeco.openpose.Matrix;
import org.bytedeco.openpose.global.openpose.Detector;
import org.bytedeco.openpose.global.openpose.PoseMode;
import org.vitrivr.cineast.core.config.PoseConfig;

public class BodyHandsOpenPose extends AbstractOpenPose {

  public BodyHandsOpenPose(PoseConfig config) {
    super(config);
  }

  @Override
  void configure() {
    configurePose(PoseMode.Enabled);
    configureHand(Detector.Body);
  }

  private static float[][][] procKps(Datum dat) {
    FloatArray poseArray = dat.poseKeypoints();
    FloatArray2 handArrays = dat.handKeypoints();
    FloatArray lHandArray = handArrays.get(0);
    FloatArray rHandArray = handArrays.get(1);
    IntPointer dimSizes = poseArray.getSize();
    if (dimSizes == null) {
      return null;
    }
    int numPeople = dimSizes.get(0);
    float[][][] keypoints = new float[numPeople][65][3];
    for (int i = 0; i < numPeople; i++) {
      for (int j = 0; j < 25; j++) {
        keypoints[i][j] = getKp(poseArray, i, j);
      }
      for (int j = 25; j < 45; j++) {
        keypoints[i][j] = getKp(lHandArray, i, j - 24);
      }
      for (int j = 45; j < 65; j++) {
        keypoints[i][j] = getKp(rHandArray, i, j - 44);
      }
    }
    return keypoints;
  }

  public float[][][] getPoses(Matrix opIm) {
    Datum dat = new Datum();
    Datums dats = new Datums();
    float[][][] keypoints;
    try {
      dat.cvInputData(opIm);
      dats.put(dat);
      this.opWrapper.emplaceAndPop(dats);
      keypoints = procKps(dat);
    } finally {
      // We don't want to deallocate opIm yet (handled by OpenPoseSession)
      dat.cvInputData(new Matrix());
      // We don't actually deallocate dat because dats will do it when deallocated
      dat.deallocate(false);
      dats.deallocate();
    }

    return keypoints;
  }
}
