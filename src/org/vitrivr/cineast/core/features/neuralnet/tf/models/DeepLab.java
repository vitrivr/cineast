package org.vitrivr.cineast.core.features.neuralnet.tf.models;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.LongBuffer;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

public class DeepLab implements AutoCloseable{

  private final Graph graph;
  private final Session session;
  private final String[] labels;

  public DeepLab(byte[] graph, String[] labels){
    this.graph = new Graph();
    this.graph.importGraphDef(graph);
    this.session = new Session(this.graph);
    this.labels = labels;
  }


  /**
   * returns the class label index for every pixel of the rescaled image
   */
  public synchronized int[][] processImage(BufferedImage img){
    Tensor<UInt8> input = prepareImage(img);
    Tensor<Long> result = session.runner().feed("ImageTensor", input)
        .fetch("SemanticPredictions").run().get(0).expect(Long.class);
    input.close();

    int len = result.numElements();
    LongBuffer buf = LongBuffer.allocate(len);
    result.writeTo(buf);
    result.close();

    long[] resultShape = result.shape();
    long[] resultArray = buf.array();

    int w = (int) resultShape[2];
    int h = (int) resultShape[1];

    int[][] resultMatrix = new int[w][h];

    for(int i = 0; i < resultArray.length; ++i){
      resultMatrix[i % w][i / w] = (int)resultArray[i];
    }

    return resultMatrix;
  }

  private static Tensor<UInt8> prepareImage(BufferedImage input) {

    float ratio = 513f / Math.max(input.getWidth(), input.getHeight());
    int w = (int)(input.getWidth() * ratio), h = (int)(input.getHeight() * ratio);

    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(input, 0, 0, w, h, null);
    g2.dispose();


    byte[][][][] bimg = new byte[1][h][w][3];

    for(int x = 0; x < resizedImg.getWidth(); ++x){
      for(int y = 0; y < resizedImg.getHeight(); ++y){
        Color c = new Color(resizedImg.getRGB(x, y));
        bimg[0][y][x][0] = (byte)(c.getRed() & 0xff);
        bimg[0][y][x][1] = (byte)(c.getGreen() & 0xff);
        bimg[0][y][x][2] = (byte)(c.getBlue() & 0xff);
      }
    }

    Tensor<UInt8> imageTensor = Tensor.create(bimg, UInt8.class);

    return imageTensor;

  }

  public int getColor(long cls){
    if(cls == 0){
      return Color.BLACK.getRGB();
    }
    return Color.HSBtoRGB((cls / (float)(this.labels.length - 1)), 0.8f, 0.8f);
  }

  @Override
  public void close() {
    this.session.close();
    this.graph.close();
  }
}
