package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.types.TFloat32;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

public class InceptionResnetV2 extends AbstractFeatureModule {

  public static final int ENCODING_SIZE = 1536;
  private static final String TABLE_NAME = "features_inceptionresnetv2";
  private static final Distance DISTANCE = ReadableQueryConfig.Distance.euclidean;

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Required dimensions of visual embedding model.
   */
  public static final int IMAGE_WIDTH = 299;
  public static final int IMAGE_HEIGHT = 299;

  /**
   * Resource paths.
   */
  private static final String MODEL_PATH = "resources/VisualTextCoEmbedding/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop";

  /**
   * Model input and output names.
   */
  public static final String INPUT = "input_1";
  public static final String OUTPUT = "global_average_pooling2d";

  /**
   * InceptionResNetV2 pretrained on ImageNet: https://storage.googleapis.com/tensorflow/keras-applications/inception_resnet_v2/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop.h5
   */
  private static SavedModelBundle model;

  public InceptionResnetV2() {
    super(TABLE_NAME, ENCODING_SIZE, ENCODING_SIZE);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    // Return if already processed
    if (phandler.idExists(shot.getId())) {
      return;
    }

    // Case: segment contains image
    if (shot.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      BufferedImage image = shot.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] encodingArray = encodeImage(image);
        this.persist(shot.getId(), new FloatVectorImpl(encodingArray));
      }

      // Insert return here if additional cases are added!
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    if (sc.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      LOGGER.error("Could not get similar because no image was provided.");
      return new ArrayList<>();
    }

    BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

    if (image == null) {
      LOGGER.error("Could not get similar because image could not be converted to BufferedImage.");
      return new ArrayList<>();
    }

    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    float[] encodingArray = encodeImage(image);

    return getSimilar(encodingArray, queryConfig);
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    return super.getSimilar(segmentId, queryConfig);
  }

  public static SavedModelBundle getModel() {
    initializeModel();

    return model;
  }

  /**
   * Encodes the given image using InceptionResnetV2.
   *
   * @return Intermediary encoding, not yet embedded.
   */
  public static float[] encodeImage(BufferedImage image) {
    initializeModel();

    float[] processedColors = preprocessImage(image);

    try (TFloat32 imageTensor = TFloat32.tensorOf(Shape.of(1, IMAGE_WIDTH, IMAGE_HEIGHT, 3), DataBuffers.of(processedColors))) {
      HashMap<String, Tensor> inputMap = new HashMap<>();
      inputMap.put(INPUT, imageTensor);

      Map<String, Tensor> resultMap = model.call(inputMap);

      try (TFloat32 encoding = (TFloat32) resultMap.get(OUTPUT)) {

        float[] embeddingArray = new float[ENCODING_SIZE];
        FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
        encoding.read(floatBuffer);

        return embeddingArray;
      }
    }
  }

  /**
   * Preprocesses the image, so it can be used as input to the InceptionResnetV2. Involves rescaling, remapping and converting the image to a float array.
   *
   * @return float array representation of the input image
   */
  public static float[] preprocessImage(BufferedImage image) {
    if (image.getWidth() != IMAGE_WIDTH || image.getHeight() != IMAGE_HEIGHT) {
      try {
        image = Thumbnails.of(image).forceSize(IMAGE_WIDTH, IMAGE_HEIGHT).asBufferedImage();
      } catch (IOException e) {
        LOGGER.error("Could not resize image", e);
      }
    }
    int[] colors = image.getRGB(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null, 0, IMAGE_WIDTH);
    int[] rgb = colorsToRGB(colors);
    return preprocessInput(rgb);
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

  /**
   * Converts an integer colors array storing ARGB values in each integer into an integer array where each integer stores R, G or B value.
   */
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

  private static void initializeModel() {
    if (model == null) {
      model = SavedModelBundle.load(MODEL_PATH);
    }
  }
}
