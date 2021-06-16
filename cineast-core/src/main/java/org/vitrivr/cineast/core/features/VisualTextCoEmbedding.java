package org.vitrivr.cineast.core.features;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

/**
 * A visual-text co-embedding mapping images and text descriptions to the same embedding space.
 */
public class VisualTextCoEmbedding extends AbstractFeatureModule {

  private static final int EMBEDDING_SIZE = 256;
  private static final String TABLE_NAME = "features_visualtextcoembedding";

  /**
   * Required dimensions of visual embedding model.
   */
  private static final int IMAGE_WIDTH = 299;
  private static final int IMAGE_HEIGHT = 299;

  /**
   * Resource paths.
   */
  private static final String RESOURCE_PATH = "resources/VisualTextCoEmbedding/";
  private static final String TEXT_EMBEDDING_MODEL = "universal-sentence-encoder_4";
  private static final String TEXT_CO_EMBEDDING_MODEL = "text-co-embedding";
  private static final String VISUAL_EMBEDDING_MODEL = "inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop";
  private static final String VISUAL_CO_EMBEDDING_MODEL = "visual-co-embedding";

  /**
   * Model input and output names.
   */
  private static final String TEXT_EMBEDDING_INPUT = "inputs";
  private static final String TEXT_EMBEDDING_OUTPUT = "outputs";
  private static final String TEXT_CO_EMBEDDING_INPUT = "textual_features";
  private static final String TEXT_CO_EMBEDDING_OUTPUT = "l2_norm";
  private static final String VISUAL_EMBEDDING_INPUT = "input_1";
  private static final String VISUAL_EMBEDDING_OUTPUT = "global_average_pooling2d";
  private static final String VISUAL_CO_EMBEDDING_INPUT = "visual_features";
  private static final String VISUAL_CO_EMBEDDING_OUTPUT = "l2_norm";

  /**
   * Embedding network from text to intermediary embedding.
   * <p>
   * Currently using UniversalSentenceEncoderV4: https://tfhub.dev/google/universal-sentence-encoder/4
   */
  private static SavedModelBundle textEmbedding;
  /**
   * Embedding network from text intermediary embedding to visual-text co-embedding.
   */
  private static SavedModelBundle textCoEmbedding;

  /**
   * Embedding network from image to intermediary embedding.
   * <p>
   * Currently using InceptionResNetV2 pretrained on ImageNet: https://storage.googleapis.com/tensorflow/keras-applications/inception_resnet_v2/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop.h5
   */
  private static SavedModelBundle visualEmbedding;
  /**
   * Embedding network from visual intermediary embedding to visual-text co-embedding.
   */
  private static SavedModelBundle visualCoEmbedding;

  public VisualTextCoEmbedding() {
    super(TABLE_NAME, 2f, EMBEDDING_SIZE);
    // TODO: Move initialization into separate methods only called when using the respective models
    // If the separation of extract from visual, query by text is strict, the models can be loaded in the respective
    // init methods.
    if (textEmbedding == null) {
      textEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_EMBEDDING_MODEL);
    }
    if (textCoEmbedding == null) {
      textCoEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_CO_EMBEDDING_MODEL);
    }

    if (visualEmbedding == null) {
      visualEmbedding = SavedModelBundle.load(RESOURCE_PATH + VISUAL_EMBEDDING_MODEL);
    }
    if (visualCoEmbedding == null) {
      visualCoEmbedding = SavedModelBundle.load(RESOURCE_PATH + VISUAL_CO_EMBEDDING_MODEL);
    }
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }

    BufferedImage image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();

    if (image != null) {
      float[] embeddingArray = embedImage(image);
      this.persist(shot.getId(), new FloatVectorImpl(embeddingArray));
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    String text = sc.getText();

    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(ReadableQueryConfig.Distance.euclidean);

    float[] embeddingArray = embedText(text);

    return getSimilar(embeddingArray, queryConfig);
  }

  private float[] embedText(String text) {
    TString textTensor = TString.tensorOf(NdArrays.vectorOfObjects(text));

    HashMap<String, Tensor> inputMap = new HashMap<>();
    inputMap.put(TEXT_EMBEDDING_INPUT, textTensor);

    Map<String, Tensor> resultMap = textEmbedding.call(inputMap);

    TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(TEXT_EMBEDDING_OUTPUT);

    inputMap.clear();
    inputMap.put(TEXT_CO_EMBEDDING_INPUT, intermediaryEmbedding);

    resultMap = textCoEmbedding.call(inputMap);
    TFloat32 embedding = (TFloat32) resultMap.get(TEXT_CO_EMBEDDING_OUTPUT);

    float[] embeddingArray = new float[EMBEDDING_SIZE];
    FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
    // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
    embedding.read(floatBuffer);
    // Close tensors manually
    textTensor.close();
    intermediaryEmbedding.close();
    embedding.close();

    // TODO: Also convert into auto-closing try-catch blocks

    return embeddingArray;
  }

  private float[] embedImage(BufferedImage image) {
    if (image.getWidth() != IMAGE_WIDTH || image.getHeight() != IMAGE_HEIGHT) {
      image = rescale(image, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
    int[] colors = image.getRGB(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null, 0, IMAGE_WIDTH);
    int[] rgb = colorsToRGB(colors);
    float[] processedColors = preprocessInput(rgb);

    try (TFloat32 imageTensor = TFloat32.tensorOf(Shape.of(1, IMAGE_WIDTH, IMAGE_HEIGHT, 3), DataBuffers.of(processedColors))) {
      HashMap<String, Tensor> inputMap = new HashMap<>();
      inputMap.put(VISUAL_EMBEDDING_INPUT, imageTensor);

      Map<String, Tensor> resultMap = visualEmbedding.call(inputMap);

      try (TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(VISUAL_EMBEDDING_OUTPUT)) {

        inputMap.clear();
        inputMap.put(VISUAL_CO_EMBEDDING_INPUT, intermediaryEmbedding);

        resultMap = visualCoEmbedding.call(inputMap);
        try (TFloat32 embedding = (TFloat32) resultMap.get(VISUAL_CO_EMBEDDING_OUTPUT)) {

          float[] embeddingArray = new float[EMBEDDING_SIZE];
          FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
          // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
          embedding.read(floatBuffer);

          return embeddingArray;
        }
      }
    }
  }

  /**
   * Preprocesses input in a way equivalent to that performed in the Python TensorFlow library.
   * <p>
   * Maps all values from [0,255] to [-1, 1].
   */
  private static float[] preprocessInput(int[] colors) {
    // x /= 127.5
    // x -= 1.
    float[] processedColors = new float[colors.length];
    for (int i = 0; i < colors.length; i++) {
      processedColors[i] = (colors[i] / 127.5f) - 1;
    }

    return processedColors;
  }

  private static int[] colorsToRGB(int[] colors) {
    int[] rgb = new int[colors.length * 3];

    for (int i = 0; i < colors.length; i++) {
      // Start index for rgb array
      int j = i * 3;
      rgb[j] = (colors[i] >> 16) & 0xFF; // r
      rgb[j + 1] = (colors[i] >> 8) & 0xFF; // g
      rgb[j + 2] = colors[i] & 0xFF; // b
    }

    return rgb;
  }

  private static BufferedImage rescale(BufferedImage image, int width, int height) {
    BufferedImage scaledImage = new BufferedImage(width, height, image.getType());

    AffineTransform affineTransform = AffineTransform.getScaleInstance((double) width / image.getWidth(), (double) height / image.getHeight());
    // The OpenCV resize with which the training data was scaled defaults to bilinear interpolation
    AffineTransformOp transformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
    scaledImage = transformOp.filter(image, scaledImage);

    return scaledImage;
  }
}
