package org.vitrivr.cineast.core.pose;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.openpose.Datum;
import org.bytedeco.openpose.Datums;
import org.bytedeco.openpose.FloatArray;
import org.bytedeco.openpose.FloatArray2;
import org.bytedeco.openpose.FloatRectangle;
import org.bytedeco.openpose.FloatRectangle2;
import org.bytedeco.openpose.Matrix;
import org.bytedeco.openpose.global.openpose.Detector;
import org.bytedeco.openpose.global.openpose.PoseMode;
import org.vitrivr.cineast.core.config.PoseConfig;

public class HandOpenPose extends AbstractOpenPose {
  private final FloatRectangle NULL_RECT = new FloatRectangle(0.0f, 0.0f, 0.0f, 0.0f);

  public HandOpenPose(PoseConfig config) {
    super(config);
  }

  @Override
  void configure() {
    configurePose(PoseMode.Disabled);
    configureHand(Detector.Provided);
  }

  private static float[][][] procHandKps(Datum dat, boolean isLeft) {
    FloatArray2 handArrays = dat.handKeypoints();
    FloatArray handArray;
    if (isLeft) {
      handArray = handArrays.get(0);
    } else {
      handArray = handArrays.get(1);
    }
    IntPointer dimSizes = handArray.getSize();
    if (dimSizes == null) {
      return null;
    }
    int numPeople = dimSizes.get(0);
    float[][][] keypoints = new float[numPeople][65][3];
    for (int i = 0; i < numPeople; i++) {
      if (isLeft) {
        for (int j = 25; j < 45; j++) {
          keypoints[i][j] = getKp(handArray, i, j - 24);
        }
      } else {
        for (int j = 45; j < 65; j++) {
          keypoints[i][j] = getKp(handArray, i, j - 44);
        }
      }
    }
    return keypoints;
  }

  public float[][] getHand(Matrix opIm, float[] bbox, boolean isLeft) {
    FloatRectangle handRect = new FloatRectangle(bbox[0], bbox[1], bbox[2], bbox[3]);
    FloatRectangle2 handRects = new FloatRectangle2();
    if (isLeft) {
      handRects.put(0, handRect);
      handRects.put(1, NULL_RECT);
    } else {
      handRects.put(0, NULL_RECT);
      handRects.put(1, handRect);
    }
    Datum dat = new Datum();
    Datums dats = new Datums();
    float[][][] keypoints;
    try {
      dat.cvInputData(opIm);
      dat.handRectangles(handRects);
      dats.put(dat);
      this.opWrapper.emplaceAndPop(dats);
      keypoints = procHandKps(dat, isLeft);
    } finally {
      // We don't want to deallocate opIm yet (handled by OpenPoseSession)
      dat.cvInputData(new Matrix());
      // We want to manually deallocate handRects
      dat.handRectangles(new FloatRectangle2(new Pointer()));
      // We don't actually deallocate dat because dats will do it when deallocated
      dat.deallocate(false);
      dats.deallocate();
      // Avoid deallocating NULL_RECT
      handRects.empty();
      handRect.deallocate();
      handRects.deallocate();
    }

    if (keypoints != null && keypoints.length > 0) {
      return keypoints[0];
    } else {
      return null;
    }
  }

  @Override
  public void close() {
    super.close();
    this.NULL_RECT.deallocate();
  }
}
