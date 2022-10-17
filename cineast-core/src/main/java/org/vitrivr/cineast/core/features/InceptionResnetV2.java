package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.types.TFloat32;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.images.ImagePreprocessingHelper;

public class InceptionResnetV2 extends AbstractFeatureModule {

  public static final int ENCODING_SIZE = 1536;
  /**
   * Required dimensions of visual embedding model.
   */
  public static final int IMAGE_WIDTH = 299;
  public static final int IMAGE_HEIGHT = 299;
  /**
   * Model input and output names.
   */
  public static final String INPUT = "input_1";
  public static final String OUTPUT = "global_average_pooling2d";
  private static final String TABLE_NAME = "features_inceptionresnetv2";
  private static final Distance DISTANCE = Distance.manhattan;
  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * mean and std for color values
   */
  private static final float[] MEAN = new float[]{0.5f, 0.5f, 0.5f};
  private static final float[] STD = new float[]{0.5f, 0.5f, 0.5f};
  /**
   * Resource paths.
   */
  private static final String MODEL_PATH = "resources/VisualTextCoEmbedding/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop";
  /**
   * InceptionResNetV2 pretrained on ImageNet: https://storage.googleapis.com/tensorflow/keras-applications/inception_resnet_v2/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop.h5
   */
  private static SavedModelBundle model;

  public InceptionResnetV2() {
    super(TABLE_NAME, ENCODING_SIZE, ENCODING_SIZE);
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

      var resultMap = model.call(inputMap);
      try (TFloat32 encoding = (TFloat32) resultMap.get(OUTPUT)) {
        var embeddingArray = new float[ENCODING_SIZE];
        var floatBuffer = DataBuffers.of(embeddingArray);
        encoding.read(floatBuffer);
        return embeddingArray;
      }
    }
  }

  /**
   * Encodes each frame of the given video using InceptionResnetV2 and returns the mean encoding as float array.
   *
   * @param frames List of frames in the video or shot to be encoded.
   * @return Mean of frame encodings as float array.
   */
  public static float[] encodeVideo(List<MultiImage> frames) {
    List<float[]> encodings = frames.stream().map(image -> encodeImage(image.getBufferedImage())).collect(Collectors.toList());

    // Sum
    float[] meanEncoding = encodings.stream().reduce(new float[ENCODING_SIZE], (encoding0, encoding1) -> {
      float[] tempSum = new float[ENCODING_SIZE];

      for (int i = 0; i < ENCODING_SIZE; i++) {
        tempSum[i] = encoding0[i] + encoding1[i];
      }

      return tempSum;
    });

    // Calculate mean
    for (int i = 0; i < ENCODING_SIZE; i++) {
      meanEncoding[i] /= encodings.size();
    }

    return meanEncoding;
  }

  /**
   * Preprocesses the image, so it can be used as input to the InceptionResnetV2. Involves rescaling, remapping and converting the image to a float array.
   *
   * @return Float array representation of the input image.
   */
  public static float[] preprocessImage(BufferedImage image) {
    BufferedImage img = ImagePreprocessingHelper.forceScale(image, IMAGE_WIDTH, IMAGE_HEIGHT);
    return ImagePreprocessingHelper.imageToHWCArray(img, MEAN, STD);
  }

  private static void initializeModel() {
    if (model == null) {
      model = SavedModelBundle.load(MODEL_PATH);
    }
  }

  @Override
  public void processSegment(SegmentContainer sc) {
    // Return if already processed
    if (phandler.idExists(sc.getId())) {
      return;
    }

    // Case: segment contains video frames
    if (!sc.getVideoFrames().isEmpty() && sc.getVideoFrames().get(0) != VideoFrame.EMPTY_VIDEO_FRAME) {
      List<MultiImage> frames = sc.getVideoFrames().stream()
          .map(VideoFrame::getImage)
          .collect(Collectors.toList());

      float[] encodingArray = encodeVideo(frames);
      this.persist(sc.getId(), new FloatVectorImpl(encodingArray));

      return;
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] encodingArray = encodeImage(image);
        this.persist(sc.getId(), new FloatVectorImpl(encodingArray));
      }

      // Insert return here if additional cases are added!
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    float[] encodingArray = null;

    if (!sc.getVideoFrames().isEmpty() && sc.getVideoFrames().get(0) != VideoFrame.EMPTY_VIDEO_FRAME) {
      // Case: segment contains video frames
      List<MultiImage> frames = sc.getVideoFrames().stream()
          .map(VideoFrame::getImage)
          .collect(Collectors.toList());

      encodingArray = encodeVideo(frames);
    } else if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      // Case: segment contains image
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        encodingArray = encodeImage(image);
      } else {
        LOGGER.error("Could not get similar because image could not be converted to BufferedImage.");
      }
    }

    if (encodingArray == null) {
      LOGGER.error("Could not get similar because no acceptable modality was provided.");
      return new ArrayList<>();
    }

    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    return getSimilar(encodingArray, queryConfig);
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    return super.getSimilar(segmentId, queryConfig);
  }
}
