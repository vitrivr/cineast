package org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo;


import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.features.neuralnet.tf.GraphBuilder;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo.util.BoundingBox;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo.util.BoxPosition;
import org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo.util.Recognition;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.MathHelper.ArgMaxResult;


/**
 * based on https://github.com/szaza/tensorflow-example-java
 */
public class YOLO implements AutoCloseable {

  private final static float OVERLAP_THRESHOLD = 0.5f;
  private final static double anchors[] = {1.08, 1.19, 3.42, 4.41, 6.63, 11.38, 9.42, 5.11, 16.62,
      10.52};
  private final static int SIZE = 13;
  private final static int MAX_RECOGNIZED_CLASSES = 24;
  private final static float THRESHOLD = 0.5f;
  private final static int MAX_RESULTS = 24;
  private final static int NUMBER_OF_BOUNDING_BOX = 5;
  private static final Logger LOGGER = LogManager.getLogger();
  private final String[] LABELS = {
      "aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow",
      "diningtable", "dog", "horse", "motorbike", "person", "pottedplant", "sheep", "sofa", "train",
      "tvmonitor"
  };
  private final Graph preprocessingGraph;
  private final Session preprocessingSession;
  private final String imageOutName;
  private final Graph yoloGraph;
  private final Session yoloSession;

  public YOLO() {
    byte[] GRAPH_DEF = new byte[0];
    try {
      GRAPH_DEF = Files
          .readAllBytes((Paths.get("resources/YOLO/yolo-voc.pb")));
    } catch (IOException e) {
      throw new RuntimeException(
          "could not load graph for YOLO: " + LogHelper.getStackTrace(e));
    }
    yoloGraph = new Graph();
    yoloGraph.importGraphDef(GRAPH_DEF);
    yoloSession = new Session(yoloGraph);

    preprocessingGraph = new Graph();

    GraphBuilder graphBuilder = new GraphBuilder(preprocessingGraph);

    Output<Float> imageFloat = graphBuilder.placeholder("T", Float.class);

    final int[] size = new int[]{416, 416};

    final Output<Float> output =

        graphBuilder.resizeBilinear( // Resize using bilinear interpolation
            graphBuilder.expandDims( // Increase the output tensors dimension
                imageFloat,
                graphBuilder.constant("make_batch", 0)),
            graphBuilder.constant("size", size)
        );

    imageOutName = output.op().name();

    preprocessingSession = new Session(preprocessingGraph);

  }

  private static Tensor<Float> readImage(MultiImage img) {

    float[][][] fimg = new float[img.getHeight()][img.getWidth()][3];

    int[] colors = img.getColors();

    for (int x = 0; x < img.getWidth(); ++x) {
      for (int y = 0; y < img.getHeight(); ++y) {
        int c = colors[x + img.getWidth() * y];
        fimg[y][x][0] = ((float) RGBContainer.getRed(c)) / 255f;
        fimg[y][x][1] = ((float) RGBContainer.getGreen(c)) / 255f;
        fimg[y][x][2] = ((float) RGBContainer.getBlue(c)) / 255f;
      }
    }

    return Tensor.create(fimg, Float.class);
  }

  /**
   * Gets the number of classes based on the tensor shape
   *
   * @param result - the tensorflow output
   * @return the number of classes
   */
  private int getOutputSizeByShape(Tensor<Float> result) {
    return (int) (result.shape()[3] * Math.pow(SIZE, 2));
  }

  /**
   * It classifies the object/objects on the image
   *
   * @param tensorFlowOutput output from the TensorFlow, it is a 13x13x((num_class +1) * 5) tensor
   * 125 = (numClass +  Tx, Ty, Tw, Th, To) * 5 - cause we have 5 boxes per each cell
   * @param labels a string vector with the labels
   * @return a list of recognition objects
   */
  private List<Recognition> classifyImage(final float[] tensorFlowOutput, final String[] labels) {
    int numClass = (int) (tensorFlowOutput.length / (Math.pow(SIZE, 2) * NUMBER_OF_BOUNDING_BOX)
        - 5);
    BoundingBox[][][] boundingBoxPerCell = new BoundingBox[SIZE][SIZE][NUMBER_OF_BOUNDING_BOX];
    PriorityQueue<Recognition> priorityQueue = new PriorityQueue<>(
        MAX_RECOGNIZED_CLASSES,
        new RecognitionComparator());

    int offset = 0;
    for (int cy = 0; cy < SIZE; cy++) {        // SIZE * SIZE cells
      for (int cx = 0; cx < SIZE; cx++) {
        for (int b = 0; b < NUMBER_OF_BOUNDING_BOX; b++) {   // 5 bounding boxes per each cell
          boundingBoxPerCell[cx][cy][b] = getModel(tensorFlowOutput, cx, cy, b, numClass, offset);
          calculateTopPredictions(boundingBoxPerCell[cx][cy][b], priorityQueue, labels);
          offset = offset + numClass + 5;
        }
      }
    }

    return getRecognition(priorityQueue);
  }

  private BoundingBox getModel(final float[] tensorFlowOutput, int cx, int cy, int b, int numClass,
      int offset) {
    BoundingBox model = new BoundingBox();
    Sigmoid sigmoid = new Sigmoid();
    model.setX((cx + sigmoid.value(tensorFlowOutput[offset])) * 32);
    model.setY((cy + sigmoid.value(tensorFlowOutput[offset + 1])) * 32);
    model.setWidth(Math.exp(tensorFlowOutput[offset + 2]) * anchors[2 * b] * 32);
    model.setHeight(Math.exp(tensorFlowOutput[offset + 3]) * anchors[2 * b + 1] * 32);
    model.setConfidence(sigmoid.value(tensorFlowOutput[offset + 4]));

    model.setClasses(new double[numClass]);

    for (int probIndex = 0; probIndex < numClass; probIndex++) {
      model.getClasses()[probIndex] = tensorFlowOutput[probIndex + offset + 5];
    }

    return model;
  }

  private void calculateTopPredictions(final BoundingBox boundingBox,
      final PriorityQueue<Recognition> predictionQueue,
      final String[] labels) {
    for (int i = 0; i < boundingBox.getClasses().length; i++) {

      ArgMaxResult argMax = MathHelper.argMax(MathHelper.softmax(boundingBox.getClasses()));
      double confidenceInClass = argMax.getMaxValue() * boundingBox.getConfidence();
      if (confidenceInClass > THRESHOLD) {
        predictionQueue.add(
            new Recognition(argMax.getIndex(), labels[argMax.getIndex()], (float) confidenceInClass,
                new BoxPosition((float) (boundingBox.getX() - boundingBox.getWidth() / 2),
                    (float) (boundingBox.getY() - boundingBox.getHeight() / 2),
                    (float) boundingBox.getWidth(),
                    (float) boundingBox.getHeight())));
      }
    }
  }

  private List<Recognition> getRecognition(final PriorityQueue<Recognition> priorityQueue) {
    ArrayList<Recognition> recognitions = new ArrayList<>();

    if (priorityQueue.size() > 0) {
      // Best recognition
      Recognition bestRecognition = priorityQueue.poll();
      recognitions.add(bestRecognition);

      for (int i = 0; i < Math.min(priorityQueue.size(), MAX_RESULTS); ++i) {
        Recognition recognition = priorityQueue.poll();
        boolean overlaps = false;
        for (Recognition previousRecognition : recognitions) {
          overlaps = overlaps || (getIntersectionProportion(previousRecognition.getLocation(),
              recognition.getLocation()) > OVERLAP_THRESHOLD);
        }

        if (!overlaps) {
          recognitions.add(recognition);
        }
      }
    }

    return recognitions;
  }

  private float getIntersectionProportion(BoxPosition primaryShape, BoxPosition secondaryShape) {
    if (BoxPosition.overlaps(primaryShape, secondaryShape)) {
      float intersectionSurface = Math.max(0,
          Math.min(primaryShape.getRight(), secondaryShape.getRight()) - Math
              .max(primaryShape.getLeft(), secondaryShape.getLeft())) *
          Math.max(0, Math.min(primaryShape.getBottom(), secondaryShape.getBottom()) - Math
              .max(primaryShape.getTop(), secondaryShape.getTop()));

      float surfacePrimary = Math.abs(primaryShape.getRight() - primaryShape.getLeft()) * Math
          .abs(primaryShape.getBottom() - primaryShape.getTop());

      return intersectionSurface / surfacePrimary;
    }

    return 0f;

  }

  @Override
  public void close() {
    yoloSession.close();
    yoloGraph.close();
    preprocessingSession.close();
    preprocessingGraph.close();
  }


  public List<Recognition> detect(MultiImage img) {

    try (Tensor<Float> normalizedImage = normalizeImage(img)) {
      return classifyImage(executeYOLOGraph(normalizedImage), LABELS);
    }
  }

  /**
   * Pre-process input. It resize the image and normalize its pixels
   *
   * @return Tensor<Float> with shape [1][416][416][3]
   */
  private Tensor<Float> normalizeImage(MultiImage img) {

    try (Tensor<Float> image = readImage(img)) {
      return preprocessingSession.runner().feed("T", image).fetch(imageOutName).run().get(0)
          .expect(Float.class);
    }
  }

  /**
   * Executes graph on the given preprocessed image
   *
   * @param image preprocessed image
   * @return output tensor returned by tensorFlow
   */
  private float[] executeYOLOGraph(final Tensor<Float> image) {

    Tensor<Float> result = yoloSession.runner().feed("input", image).fetch("output").run().get(0)
        .expect(Float.class);

    float[] outputTensor = new float[getOutputSizeByShape(result)];
    FloatBuffer floatBuffer = FloatBuffer.wrap(outputTensor);
    result.writeTo(floatBuffer);
    result.close();
    return outputTensor;

  }

  // Intentionally reversed to put high confidence at the head of the queue.
  private class RecognitionComparator implements Comparator<Recognition> {

    @Override
    public int compare(final Recognition recognition1, final Recognition recognition2) {
      return Float.compare(recognition2.getConfidence(), recognition1.getConfidence());
    }
  }
}

