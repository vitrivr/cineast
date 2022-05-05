package org.vitrivr.cineast.core.util.pose;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.ConcreteFunction;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;
import org.vitrivr.cineast.core.data.Skeleton;


/**
 * Detects up to 6 complete poses see https://tfhub.dev/google/movenet/multipose/lightning/1
 */
public class MovenetMultiposeDetector implements PoseDetector {

  private static final String RESOURCE_PATH = "resources/movenet_multipose_lightning/";
  private static final String FUNCTION = "serving_default";

  private final SavedModelBundle multiPose;
  private final ConcreteFunction function;

  public MovenetMultiposeDetector() {
    this.multiPose = SavedModelBundle.load(RESOURCE_PATH);
    this.function = this.multiPose.graph().getFunction(FUNCTION);
  }


  public List<Skeleton> detectPoses(BufferedImage img) {

    final int imageSize = 256;
    float scaling = ((float) imageSize) / Math.max(img.getWidth(), img.getHeight());
    int xOffset = (int) ((imageSize - (img.getWidth() * scaling)) / 2f);
    int yOffset = (int) ((imageSize - (img.getHeight() * scaling)) / 2f);

    BufferedImage resizedImg;
    if (img.getWidth() == imageSize && img.getHeight() == imageSize) {
      resizedImg = img;
    } else {
      resizedImg = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2 = resizedImg.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.setColor(Color.white);
      g2.fillRect(0, 0, imageSize, imageSize);
      g2.drawImage(img, xOffset, yOffset, (int) (img.getWidth() * scaling), (int) (img.getHeight() * scaling), null);
      g2.dispose();
    }

    int[] colors = resizedImg.getRGB(0, 0, imageSize, imageSize, null, 0, imageSize);
    int[] rgb = new int[imageSize * imageSize * 3];

    for (int i = 0; i < colors.length; ++i) {
      int j = i * 3;
      rgb[j] = (colors[i] >> 16) & 0xFF; // r
      rgb[j + 1] = (colors[i] >> 8) & 0xFF; // g
      rgb[j + 2] = (colors[i] & 0xFF); //b
    }

    float[] points = new float[6 * 56];

    try (Tensor imageTensor = TInt32.tensorOf(Shape.of(1, imageSize, imageSize, 3), DataBuffers.of(rgb))) {
      TFloat32 pointsTensor = (TFloat32) this.function.call(imageTensor);
      FloatDataBuffer floatBuffer = DataBuffers.of(points);
      pointsTensor.read(floatBuffer);
      pointsTensor.close();
    }

    final int resultLength = 56;

    ArrayList<Skeleton> skeletons = new ArrayList<>(6);

    for (int pose = 0; pose < 6; ++pose) {

      int offset = pose * resultLength;

      float score = points[offset + 55];

      if (score < 0.5f) {
        continue;
      }

      float[] coords = new float[17 * 2];
      float[] weights = new float[17];

      for (int i = 0; i < 17; ++i) {
        coords[2 * i + 1] = ((points[offset + 3 * i] * imageSize) - yOffset) / scaling;
        coords[2 * i] = ((points[offset + 3 * i + 1] * imageSize) - xOffset) / scaling;
        weights[i] = points[offset + 3 * i + 1];
      }

      skeletons.add(new Skeleton(coords, weights));

    }

    return skeletons;

  }

}
