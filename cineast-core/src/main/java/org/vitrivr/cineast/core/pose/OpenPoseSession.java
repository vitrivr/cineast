package org.vitrivr.cineast.core.pose;

import static org.bytedeco.openpose.global.openpose.OP_CV2OPCONSTMAT;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.openpose.Matrix;
import org.opencv.core.CvType;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public class OpenPoseSession implements AutoCloseable {

  /**
   * The main purpose of this class is to allow preprocess the same image,
   * just once and then run OpenPose first normally and then with external hand bboxes.
   */
  private final BodyHandsOpenPose bodyHandsOpenPose;
  private final HandOpenPose handOpenPose;
  private final Matrix opIm;

  public OpenPoseSession(BodyHandsOpenPose bodyHandsOpenPose, HandOpenPose handOpenPose, MultiImage img) {
    this.bodyHandsOpenPose = bodyHandsOpenPose;
    this.handOpenPose = handOpenPose;
    this.opIm = preprocessImage(img);
  }

  private Matrix preprocessImage(MultiImage img) {
    Mat ocvIm = bufferedImageToMat(img.getBufferedImage());
    try {
      return OP_CV2OPCONSTMAT(ocvIm);
    } finally {
      ocvIm.deallocate();
    }
  }

  private static Mat bufferedImageToMat(BufferedImage bi) {
    if (bi.getType() != BufferedImage.TYPE_3BYTE_BGR) {
      BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      convertedImg.getGraphics().drawImage(bi, 0, 0, null);
      bi = convertedImg;
    }
    byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    return new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3, new BytePointer(ByteBuffer.wrap(data)));
  }

  public float[][][] getPoses() {
    return this.bodyHandsOpenPose.getPoses(this.opIm);
  }

  public float[][] getHand(float[] bbox, boolean isLeft) {
    return this.handOpenPose.getHand(this.opIm, bbox, isLeft);
  }

  @Override
  public void close() {
    this.opIm.deallocate();
  }
}
