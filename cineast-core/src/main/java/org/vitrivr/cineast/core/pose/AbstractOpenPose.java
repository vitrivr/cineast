package org.vitrivr.cineast.core.pose;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.bytedeco.openpose.FloatArray;
import org.bytedeco.openpose.IntPoint;
import org.bytedeco.openpose.OpString;
import org.bytedeco.openpose.OpWrapper;
import org.bytedeco.openpose.WrapperStructHand;
import org.bytedeco.openpose.WrapperStructPose;
import org.bytedeco.openpose.global.openpose.Detector;
import org.bytedeco.openpose.global.openpose.PoseMode;
import org.bytedeco.openpose.global.openpose.ThreadManagerMode;
import org.vitrivr.cineast.core.config.PoseConfig;

abstract class AbstractOpenPose implements AutoCloseable {
  protected final OpWrapper opWrapper;
  protected final PoseConfig config;

  public AbstractOpenPose(PoseConfig config) {
    this.config = config;
    // Configure OpenPose
    this.opWrapper = new OpWrapper(ThreadManagerMode.Asynchronous);
    this.opWrapper.disableMultiThreading();
    this.configure();
    // Start OpenPose
    this.opWrapper.start();
  }

  protected void configurePose(PoseMode poseMode) {
    String modelPath = this.config.getModelPath();
    if (modelPath == null) {
      throw new UncheckedIOException(new IOException("modelPath cannot be null"));
    }
    WrapperStructPose structPose = new WrapperStructPose();
    structPose.poseMode(poseMode);
    structPose.modelFolder(new OpString(modelPath));
    PoseConfig.Resolution bodyNetResolution = this.config.getBodyNetResolution();
    if (bodyNetResolution != null) {
      structPose.netInputSize(convRes(bodyNetResolution));
    }
    this.opWrapper.configure(structPose);
  }

  protected void configureHand(Detector detector) {
    WrapperStructHand structHand = new WrapperStructHand();
    structHand.enable(true);
    PoseConfig.Resolution handNetResolution = this.config.getHandNetResolution();
    if (handNetResolution != null) {
      structHand.netInputSize(convRes(handNetResolution));
    }
    structHand.detector(detector);
    this.opWrapper.configure(structHand);
  }

  abstract void configure();

  private static IntPoint convRes(PoseConfig.Resolution res) {
    return new IntPoint(res.x, res.y);
  }

  /*public OpenPoseSession start(MultiImage img) {
    return new OpenPoseSession(this, img);
  }*/

  protected static float[] getKp(FloatArray arr, int i, int j) {
    return new float[] {
        arr.get(new int[]{i, j, 0})[0],
        arr.get(new int[]{i, j, 1})[0],
        arr.get(new int[]{i, j, 2})[0]
    };
  }

  @Override
  public void close() {
    this.opWrapper.close();
    this.opWrapper.deallocate();
  }
}
