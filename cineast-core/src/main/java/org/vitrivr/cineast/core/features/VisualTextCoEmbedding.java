package org.vitrivr.cineast.core.features;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

/**
 * A visual-text co-embedding mapping images and text descriptions to the same embedding space.
 */
public class VisualTextCoEmbedding extends AbstractFeatureModule {

  private static final int EMBEDDING_SIZE = 256;
  private static final String TABLE_NAME = "features_visualtextcoembedding";
  private static final Distance DISTANCE = ReadableQueryConfig.Distance.euclidean;

  /**
   * Resource paths.
   */
  private static final String RESOURCE_PATH = "resources/VisualTextCoEmbedding/";
  private static final String TEXT_EMBEDDING_MODEL = "universal-sentence-encoder_4";
  private static final String TEXT_CO_EMBEDDING_MODEL = "text-co-embedding";
  private static final String VISUAL_CO_EMBEDDING_MODEL = "visual-co-embedding";

  /**
   * Model input and output names.
   */
  private static final String TEXT_EMBEDDING_INPUT = "inputs";
  private static final String TEXT_EMBEDDING_OUTPUT = "outputs";
  private static final String TEXT_CO_EMBEDDING_INPUT = "textual_features";
  private static final String TEXT_CO_EMBEDDING_OUTPUT = "l2_norm";
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

      float[] embeddingArray = embedVideo(frames);
      this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));

      return;
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] embeddingArray = embedImage(image);
        this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
      }

      // Insert return here if additional cases are added!
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    String text = sc.getText();

    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    float[] embeddingArray = embedText(text);

    return getSimilar(embeddingArray, queryConfig);
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    return super.getSimilar(segmentId, queryConfig);
  }

  private void initializeTextEmbedding() {
    if (textEmbedding == null) {
      textEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_EMBEDDING_MODEL);
    }
    if (textCoEmbedding == null) {
      textCoEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_CO_EMBEDDING_MODEL);
    }
  }

  private void initializeVisualEmbedding() {
    if (visualEmbedding == null) {
      visualEmbedding = InceptionResnetV2.getModel();
    }
    if (visualCoEmbedding == null) {
      visualCoEmbedding = SavedModelBundle.load(RESOURCE_PATH + VISUAL_CO_EMBEDDING_MODEL);
    }
  }

  private float[] embedText(String text) {
    initializeTextEmbedding();

    try (TString textTensor = TString.tensorOf(NdArrays.vectorOfObjects(text))) {

      HashMap<String, Tensor> inputMap = new HashMap<>();
      inputMap.put(TEXT_EMBEDDING_INPUT, textTensor);

      Map<String, Tensor> resultMap = textEmbedding.call(inputMap);

      try (TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(TEXT_EMBEDDING_OUTPUT)) {

        inputMap.clear();
        inputMap.put(TEXT_CO_EMBEDDING_INPUT, intermediaryEmbedding);

        resultMap = textCoEmbedding.call(inputMap);
        try (TFloat32 embedding = (TFloat32) resultMap.get(TEXT_CO_EMBEDDING_OUTPUT)) {

          float[] embeddingArray = new float[EMBEDDING_SIZE];
          FloatDataBuffer floatBuffer = DataBuffers.of(embeddingArray);
          // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
          embedding.read(floatBuffer);

          return embeddingArray;
        }
      }
    }
  }

  private float[] embedImage(BufferedImage image) {
    initializeVisualEmbedding();

    float[] processedColors = InceptionResnetV2.preprocessImage(image);

    try (TFloat32 imageTensor = TFloat32.tensorOf(Shape.of(1, InceptionResnetV2.IMAGE_WIDTH, InceptionResnetV2.IMAGE_HEIGHT, 3), DataBuffers.of(processedColors))) {
      HashMap<String, Tensor> inputMap = new HashMap<>();
      inputMap.put(InceptionResnetV2.INPUT, imageTensor);

      Map<String, Tensor> resultMap = visualEmbedding.call(inputMap);

      try (TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(InceptionResnetV2.OUTPUT)) {

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

  private float[] embedVideo(List<MultiImage> frames) {
    initializeVisualEmbedding();

    float[] meanEncoding = InceptionResnetV2.encodeVideo(frames);

    try (TFloat32 encoding = TFloat32.tensorOf(Shape.of(1, InceptionResnetV2.ENCODING_SIZE), DataBuffers.of(meanEncoding))) {
      HashMap<String, Tensor> inputMap = new HashMap<>();

      inputMap.put(VISUAL_CO_EMBEDDING_INPUT, encoding);

      Map<String, Tensor> resultMap = visualCoEmbedding.call(inputMap);
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
