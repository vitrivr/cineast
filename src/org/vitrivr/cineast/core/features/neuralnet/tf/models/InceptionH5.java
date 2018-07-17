package org.vitrivr.cineast.core.features.neuralnet.tf.models;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Session.Runner;
import org.tensorflow.Tensor;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.features.neuralnet.tf.GraphBuilder;
import org.vitrivr.cineast.core.features.neuralnet.tf.GraphHelper;
import org.vitrivr.cineast.core.util.LogHelper;

public class InceptionH5 implements AutoCloseable {

  private final Graph classificationGraph, preprocessingGraph;
  private final Session classificationSession, preProcessingSession;
  private final Output<Float> imageFloat, output;
  private final ArrayList<String> outputOperations;

  public InceptionH5() {
    this(null);
  }

  public InceptionH5(List<String> outputOperations) {

    byte[] graphDef = new byte[0];
    try {
      graphDef = Files.readAllBytes((Paths.get("tensorflow_inception_graph.pb"))); //TODO configure path
    } catch (IOException e) {
      throw new RuntimeException("could not load graph for InceptionH5: " + LogHelper.getStackTrace(e));
    }
    classificationGraph = new Graph();
    classificationGraph.importGraphDef(graphDef);
    classificationSession = new Session(classificationGraph);

    preprocessingGraph = new Graph();
    GraphBuilder b = new GraphBuilder(preprocessingGraph);
    preProcessingSession = new Session(preprocessingGraph);

    final int H = 224;
    final int W = 224;

    imageFloat = b.placeholder("T", Float.class);
    output =
        b.resizeBilinear(
            b.expandDims(
                imageFloat,
                b.constant("make_batch", 0)),
            b.constant("size", new int[]{H, W}));

    if (outputOperations != null && !outputOperations.isEmpty()) {
      this.outputOperations = new ArrayList<>();
      this.outputOperations.addAll(
          GraphHelper.filterOperations(outputOperations, classificationGraph)
      );

    } else {
      this.outputOperations = new ArrayList<>(1);
      this.outputOperations.add("output2"); //default output
    }

  }


  public HashMap<String, float[]> transform(MultiImage img) {

    Tensor<Float> imageTensor = readImage(img);

    Tensor<Float> image = preProcessingSession.runner().feed("T", imageTensor)
        .fetch(output.op().name()).run()
        .get(0)
        .expect(Float.class);
    imageTensor.close();

    Runner runner = classificationSession.runner().feed("input", image);

    for(String operation : this.outputOperations){
      runner.fetch(operation);
    }

    List<Tensor<?>> results = runner.run();

    HashMap<String, float[]> _return = new HashMap<>();


    for(int i = 0; i < this.outputOperations.size(); ++i){
      Tensor<Float> result = results.get(i).expect(Float.class);
      FloatBuffer floatBuffer = FloatBuffer.allocate(result.numElements());
      _return.put(this.outputOperations.get(i), floatBuffer.array());
      result.close();
    }

    return _return;

  }

  @Override
  public void close() {
    classificationSession.close();
    classificationGraph.close();
    preProcessingSession.close();
    preprocessingGraph.close();

  }

  private static Tensor<Float> readImage(MultiImage img) {
    float[][][] fimg = new float[img.getHeight()][img.getWidth()][3];

    double sum = 0d;

    int[] colors = img.getColors();

    for (int y = 0; y < img.getHeight(); ++y) {
      for (int x = 0; x < img.getWidth(); ++x) {
        int c = colors[x + img.getWidth() * y];
        fimg[y][x][0] = (float) RGBContainer.getRed(c);
        fimg[y][x][1] = (float) RGBContainer.getGreen(c);
        fimg[y][x][2] = (float) RGBContainer.getBlue(c);

        sum += fimg[y][x][0] + fimg[y][x][1] + fimg[y][x][2];

      }
    }

    sum /= (img.getWidth() * img.getHeight());

    for (int y = 0; y < img.getHeight(); ++y) {
      for (int x = 0; x < img.getWidth(); ++x) {
        fimg[y][x][0] -= sum;
        fimg[y][x][1] -= sum;
        fimg[y][x][2] -= sum;

      }
    }

    return Tensor.create(fimg, Float.class);

  }
}
