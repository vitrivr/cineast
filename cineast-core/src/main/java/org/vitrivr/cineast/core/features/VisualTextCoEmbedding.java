package org.vitrivr.cineast.core.features;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.m3d.texturemodel.IModel;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.render.lwjgl.render.RenderOptions;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.window.WindowOptions;
import org.vitrivr.cineast.core.util.KMeansPP;
import org.vitrivr.cineast.core.util.math.MathHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointStrategy;
;

/**
 * A visual-text co-embedding mapping images and text descriptions to the same embedding space.
 * <p>
 */
public class VisualTextCoEmbedding extends AbstractFeatureModule {

  private static final int EMBEDDING_SIZE = 256;
  private static final String TABLE_NAME = "features_visualtextcoembedding";
  private static final Distance DISTANCE = ReadableQueryConfig.Distance.euclidean;

  private static final Logger LOGGER = LogManager.getLogger();

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
   * Currently using <a href="https://tfhub.dev/google/universal-sentence-encoder/4">UniversalSentenceEncoderV4</a>.
   */
  private static SavedModelBundle textEmbedding;
  /**
   * Embedding network from text intermediary embedding to visual-text co-embedding.
   */
  private static SavedModelBundle textCoEmbedding;

  /**
   * Embedding network from image to intermediary embedding.
   * <p>
   * Currently using InceptionResNetV2 pretrained on <a href="https://storage.googleapis.com/tensorflow/keras-applications/inception_resnet_v2/inception_resnet_v2_weights_tf_dim_ordering_tf_kernels_notop.h5">ImageNet</a>.
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
      List<MultiImage> frames = sc.getVideoFrames().stream().map(VideoFrame::getImage).collect(Collectors.toList());

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

      return;
    }

    // Case: segment contains model
    var model = sc.getModel();
    if (model != null) {
      float[] embeddingArray = embedModel(model);
      this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
      System.gc();
      return;
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    // Case: segment contains text
    if (!sc.getText().isEmpty()) {
      String text = sc.getText();
      LOGGER.debug("Retrieving with TEXT: " + text);
      float[] embeddingArray = embedText(text);

      return getSimilar(embeddingArray, queryConfig);
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      LOGGER.debug("Retrieving with IMAGE.");
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] embeddingArray = embedImage(image);
        return getSimilar(embeddingArray, queryConfig);
      }

      LOGGER.error("Image was provided, but could not be decoded!");
    }

    // Case: segment contains model
    var model = sc.getModel();
    if (model != null) {
      LOGGER.debug("Retrieving with MODEL.");
      float[] embeddingArray = embedModel(model);
      return getSimilar(embeddingArray, queryConfig);
    }

    LOGGER.error("Could not get similar because no acceptable modality was provided.");
    return new ArrayList<>();
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(DISTANCE);

    return super.getSimilar(segmentId, queryConfig);
  }

  private synchronized static void initializeTextEmbedding() {
    if (textEmbedding == null) {
      textEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_EMBEDDING_MODEL);
    }
    if (textCoEmbedding == null) {
      textCoEmbedding = SavedModelBundle.load(RESOURCE_PATH + TEXT_CO_EMBEDDING_MODEL);
    }
  }

  private synchronized static void initializeVisualEmbedding() {
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
      var resultMap = textEmbedding.call(inputMap);
      try (TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(TEXT_EMBEDDING_OUTPUT).get()) {

        inputMap.clear();
        inputMap.put(TEXT_CO_EMBEDDING_INPUT, intermediaryEmbedding);
        resultMap = textCoEmbedding.call(inputMap);
        try (TFloat32 embedding = (TFloat32) resultMap.get(TEXT_CO_EMBEDDING_OUTPUT).get()) {
          var embeddingArray = new float[EMBEDDING_SIZE];
          var floatBuffer = DataBuffers.of(embeddingArray);
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

      Result resultMap = visualEmbedding.call(inputMap);
      try (TFloat32 intermediaryEmbedding = (TFloat32) resultMap.get(InceptionResnetV2.OUTPUT).get()) {
        inputMap.clear();
        inputMap.put(VISUAL_CO_EMBEDDING_INPUT, intermediaryEmbedding);
        resultMap = visualCoEmbedding.call(inputMap);
        try (TFloat32 embedding = (TFloat32) resultMap.get(VISUAL_CO_EMBEDDING_OUTPUT).get()) {
          var embeddingArray = new float[EMBEDDING_SIZE];
          var floatBuffer = DataBuffers.of(embeddingArray);
          // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
          embedding.read(floatBuffer);

          return embeddingArray;
        }
      }
    }
  }

  /**
   * Helper to convert images to frames
   *
   * @param images are converted to frames
   * @return List<MultiImage> for video embedding
   */
  private List<MultiImage> framesFromImages(List<BufferedImage> images) {
    var frames = new ArrayList<MultiImage>();
    for (BufferedImage image : images) {
      var factory = CachedDataFactory.getDefault();
      frames.add(factory.newInMemoryMultiImage(image));
    }
    return frames;
  }

  /**
   * This method takes a list of images and determines, based on {@link ViewpointStrategy}, the embed vector which describes the images most precise. This method can be simplified, once the best strategy is determined.
   *
   * @param images            the list of Images to embed
   * @param viewpointStrategy the strategy to find the vector
   * @return the embedding vector
   */
  private float[] embedMostRepresentativeImages(List<BufferedImage> images, ViewpointStrategy viewpointStrategy) {

    var retVal = new float[EMBEDDING_SIZE];

    switch (viewpointStrategy) {
      // Strategy for embedding multiple images. Choose mean of the most contained cluster. Project mean to unit hypersphere.
      case MULTI_IMAGE_KMEANS -> {
        var floatvectors = new ArrayList<FloatVectorImpl>();
        var vectors = embedMultipleImages(images);
        vectors.forEach(v -> floatvectors.add(new FloatVectorImpl(v)));
        var kmeans = KMeansPP.bestOfkMeansPP(floatvectors, new FloatVectorImpl(new float[EMBEDDING_SIZE]), 3, -1f, 5);
        // Find the index of thr cluster with the most elements
        int maxIndex = 0;
        for (var ic = 0; ic < kmeans.getPoints().size(); ++ic) {
          if (kmeans.getPoints().get(ic).size() > kmeans.getPoints().get(maxIndex).size()) {
            maxIndex = ic;
          }
          if (kmeans.getPoints().get(ic).size() == kmeans.getPoints().get(maxIndex).size()) {
            if (kmeans.getDistance(ic) < kmeans.getDistance(maxIndex)) {
              maxIndex = ic;
            }
          }
        }
        ReadableFloatVector.toArray(kmeans.getCenters().get(maxIndex), retVal);
        return MathHelper.normalizeL2InPlace(retVal);
      }
      // Strategy for embedding multiple images. Calculate mean over all. Project mean to unit hypersphere.
      case MULTI_IMAGE_PROJECTEDMEAN -> {
        var vectors = embedMultipleImages(images);
        var vectorsMean = new float[EMBEDDING_SIZE];
        for (var vector : vectors) {
          for (var ic = 0; ic < vector.length; ++ic) {
            vectorsMean[ic] += vector[ic] / vectors.size();
          }
        }
        return MathHelper.normalizeL2InPlace(vectorsMean);
      }
      // Strategy for embedding multiple images. Create a video from the images and embed the video.
      case MULTI_IMAGE_FRAME -> {
        var frames = framesFromImages(images);
        return embedVideo(frames);
      }
      // Strategy for embedding an image consisting out of four sub images.
      case MULTI_IMAGE_2_2 -> {
        assert images.size() == 4;
        var sz = images.get(0).getWidth();
        var size = sz * 2;
        // Combine the images into a single image.
        var canvas = new BufferedImage(
            size,
            size,
            BufferedImage.TYPE_INT_RGB);
        var graphics = canvas.getGraphics();
        graphics.setColor(Color.BLACK);
        // ic: image counter, idx: x-axis-index, idy: yaxis-index
        var ic = 0;
        for (var partialImage : images) {
          int idx = ic % 2;
          int idy = ic < 2 ? 0 : 1;
          graphics.drawImage(partialImage, idx * sz, idy * sz, null);
          ++ic;
        }
        retVal = embedImage(canvas);
      }
    }
    return retVal;
  }

  /**
   * Embeds a list of images
   *
   * @param images the list of images to embed
   * @return the list of embedding vectors
   */
  private List<float[]> embedMultipleImages(List<BufferedImage> images) {
    var vectors = new ArrayList<float[]>();
    for (BufferedImage image : images) {
      float[] embeddingArray = embedImage(image);
      vectors.add(embeddingArray);
    }
    return vectors;
  }

  /**
   * This method embeds a 3D model and returns the feature vector.
   */
  private float[] embedModel(IModel model) {
    //Options for window
    var windowOptions = new WindowOptions() {{
      this.hideWindow = true;
      this.width = 600;
      this.height = 600;
    }};
    // Options for renderer
    var renderOptions = new RenderOptions() {{
      this.showTextures = true;
      this.lightingOptions.hasNonDefaultTexture = model.usesNonDefaultTexture();
    }};
    // Select the strategy which will be used for model embedding
    var viewpointStrategy = ViewpointStrategy.MULTI_IMAGE_KMEANS;
    // Get camera viewpoint for chosen strategy
    var cameraPositions = ViewpointHelper.getCameraPositions(viewpointStrategy, model);
    // Render an image for each camera position
    var images = RenderJob.performStandardRenderJob(RenderWorker.getRenderJobQueue(),
        model, cameraPositions, windowOptions, renderOptions);

    // Embedding based on strategy return value. Empty if an error occurred
    if (images.isEmpty()) {
      return new float[EMBEDDING_SIZE];
    }
    if (images.size() == 1) {
      return embedImage(images.get(0));
    }
    return embedMostRepresentativeImages(images, viewpointStrategy);
  }


  private float[] embedVideo(List<MultiImage> frames) {
    initializeVisualEmbedding();

    float[] meanEncoding = InceptionResnetV2.encodeVideo(frames);

    try (TFloat32 encoding = TFloat32.tensorOf(Shape.of(1, InceptionResnetV2.ENCODING_SIZE), DataBuffers.of(meanEncoding))) {
      HashMap<String, Tensor> inputMap = new HashMap<>();

      inputMap.put(VISUAL_CO_EMBEDDING_INPUT, encoding);
      Result resultMap = visualCoEmbedding.call(inputMap);
      try (TFloat32 embedding = (TFloat32) resultMap.get(VISUAL_CO_EMBEDDING_OUTPUT).get()) {
        var embeddingArray = new float[EMBEDDING_SIZE];
        var floatBuffer = DataBuffers.of(embeddingArray);
        // Beware TensorFlow allows tensor writing to buffers through the function read rather than write
        embedding.read(floatBuffer);

        return embeddingArray;
      }
    }
  }
}
