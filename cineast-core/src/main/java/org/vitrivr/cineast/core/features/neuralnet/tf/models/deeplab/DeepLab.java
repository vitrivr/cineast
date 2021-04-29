package org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab;


import com.google.protobuf.InvalidProtocolBufferException;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.proto.framework.GraphDef;
import org.tensorflow.types.TInt64;
import org.tensorflow.types.TUint8;
import org.vitrivr.cineast.core.util.LogHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeepLab implements AutoCloseable {

  private final Graph graph;
  private final Session session;
  private final String[] labels;

  public DeepLab(byte[] graph, String[] labels) {
    this.graph = new Graph();
    try {
      this.graph.importGraphDef(GraphDef.parseFrom(graph));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    this.session = new Session(this.graph);
    this.labels = labels;
  }


  /**
   * returns the class label index for every pixel of the rescaled image
   */
  public synchronized int[][] processImage(BufferedImage img) {
    TUint8 input = prepareImage(img);
    int[][] _return = processImage(input);
    input.close();
    return _return;
  }


  public synchronized int[][] processImage(TUint8 input) {

    TInt64 result = (TInt64) session.runner().feed("ImageTensor", input).fetch("SemanticPredictions").run().get(0);


    int w = (int) result.shape().size(2);
    int h = (int) result.shape().size(1);

    int[][] resultMatrix = new int[w][h];

    for (int x = 0; x < w; ++x) {
      for (int y = 0; y < h; ++y) {
        resultMatrix[x][y] = (int) result.getLong(0, y, x);
      }
    }

    result.close();

    return resultMatrix;
  }

  public static TUint8 prepareImage(BufferedImage input) {

    float ratio = 513f / Math.max(input.getWidth(), input.getHeight());
    int w = (int) (input.getWidth() * ratio), h = (int) (input.getHeight() * ratio);

    BufferedImage resizedImg;
    if (input.getWidth() == w && input.getHeight() == h) {
      resizedImg = input;
    } else {
      resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
      Graphics2D g2 = resizedImg.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(input, 0, 0, w, h, null);
      g2.dispose();
    }

    TUint8 imageTensor = TUint8.tensorOf(Shape.of(1, h, w, 3));

    for (int x = 0; x < resizedImg.getWidth(); ++x) {
      for (int y = 0; y < resizedImg.getHeight(); ++y) {
        Color c = new Color(resizedImg.getRGB(x, y));
        imageTensor.setByte((byte) (c.getRed() & 0xff), 0, y, x, 0);
        imageTensor.setByte((byte) (c.getGreen() & 0xff), 0, y, x, 1);
        imageTensor.setByte((byte) (c.getBlue() & 0xff), 0, y, x, 2);
      }
    }

    return imageTensor;
  }

  public int getColor(long cls) {
    if (cls == 0) {
      return Color.BLACK.getRGB();
    }
    return Color.HSBtoRGB((cls / (float) (this.labels.length - 1)), 0.8f, 0.8f);
  }

  @Override
  public void close() {
    this.session.close();
    this.graph.close();
  }

  protected static byte[] load(String path) {
    try {
      return Files.readAllBytes((Paths.get(path)));
    } catch (IOException e) {
      throw new RuntimeException(
          "could not load graph for DeepLab: " + LogHelper.getStackTrace(e));
    }
  }
}
