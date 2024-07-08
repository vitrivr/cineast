package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.vitrivr.cineast.core.util.CineastConstants;
import org.vitrivr.cineast.core.util.KMeansPP;
import org.vitrivr.cineast.core.util.math.MathHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointHelper;
import org.vitrivr.cineast.core.util.texturemodel.Viewpoint.ViewpointStrategy;
import org.vitrivr.cineast.core.util.web.ImageParser;
import org.vitrivr.cineast.core.util.web.WebClient;

public class FESVectorFeature extends AbstractFeatureModule {

  public static final String DEFAULT_TABLE_NAME = "features_externalText";
  public static final float DEFAULT_MAX_DIST = 2.0f;
  public static final int DEFAULT_EMBEDDING_SIZE = 512;

  public static final Distance DEFAULT_DISTANCE = ReadableQueryConfig.Distance.euclidean;

  public final int embedding_size;

  public final Distance distance;

  public final String model;


  private static final Logger LOGGER = LogManager.getLogger();

  final WebClient client;

  public FESVectorFeature() {
    super(DEFAULT_TABLE_NAME, DEFAULT_MAX_DIST, DEFAULT_EMBEDDING_SIZE);
    embedding_size = DEFAULT_EMBEDDING_SIZE;
    distance = DEFAULT_DISTANCE;
    model = null;
    throw new IllegalArgumentException("no properties specified");
  }

  public FESVectorFeature(Map<String, String> properties) throws IOException {
    super(properties.getOrDefault(
        CineastConstants.ENTITY_NAME_KEY, DEFAULT_TABLE_NAME),
        Float.parseFloat(properties.getOrDefault("max_dist", String.valueOf(DEFAULT_MAX_DIST))),
        Integer.parseInt(properties.getOrDefault("embedding_size", String.valueOf(DEFAULT_EMBEDDING_SIZE)))
    );
    embedding_size = Integer.parseInt(properties.getOrDefault("embedding_size", String.valueOf(DEFAULT_EMBEDDING_SIZE)));
    distance = ReadableQueryConfig.Distance.valueOf(properties.getOrDefault("distance", DEFAULT_DISTANCE.name()));
    if(!properties.containsKey("endpoint")){
      throw new IllegalArgumentException("No endpoint specified for external client");
    }
    String endpoint = properties.get("endpoint");
    if(properties.containsKey("model")){
      this.model = properties.get("model");
    }else{
      this.model = null;
    }
    this.client = new WebClient(endpoint);
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

      float[] embeddingArray = new float[0];
      try {
        embeddingArray = embedVideo(frames);
      } catch (Exception e) {
        LOGGER.error("Error during extraction", e);
      }
      this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));

      return;
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] embeddingArray = new float[0];
        try {
          embeddingArray = embedImage(image);
        } catch (Exception e) {
          LOGGER.error("Error during extraction", e);
        }
        this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
      }

      return;
    }

    // Case: segment contains model
    var model = sc.getModel();
    if (model != null) {
      float[] embeddingArray = new float[0];
      try {
        embeddingArray = embedModel(model);
      } catch (Exception e) {
        LOGGER.error("Error during extraction", e);
      }
      this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
      System.gc();
      return;
    }
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(distance);

    // Case: segment contains text
    if (!sc.getText().isEmpty()) {
      String text = sc.getText();
      LOGGER.debug("Retrieving with TEXT: " + text);
      float[] embeddingArray = new float[0];
      try {
        embeddingArray = embedText(text);
      } catch (Exception e) {
        LOGGER.error("Error during extraction", e);
      }

      return getSimilar(embeddingArray, queryConfig);
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      LOGGER.debug("Retrieving with IMAGE.");
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] embeddingArray = new float[0];
        try {
          embeddingArray = embedImage(image);
        } catch (Exception e) {
          LOGGER.error("Error during extraction", e);
        }
        return getSimilar(embeddingArray, queryConfig);
      }

      LOGGER.error("Image was provided, but could not be decoded!");
    }

    // Case: segment contains model
    var model = sc.getModel();
    if (model != null) {
      LOGGER.debug("Retrieving with MODEL.");
      float[] embeddingArray = new float[0];
      try {
        embeddingArray = embedModel(model);
      } catch (Exception e) {
        LOGGER.error("Error during extraction", e);
      }
      return getSimilar(embeddingArray, queryConfig);
    }

    LOGGER.error("Could not get similar because no acceptable modality was provided.");
    return new ArrayList<>();
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    // Ensure the correct distance function is used
    QueryConfig queryConfig = QueryConfig.clone(qc);
    queryConfig.setDistance(distance);

    return super.getSimilar(segmentId, queryConfig);
  }


  private float[] embedText(String text) throws IOException, InterruptedException {
    String request;
    if (model == null){
      String template = """
          {"task":"text_embedding", "text":"%s"}
          """;
      request = String.format(template, text);
    }else{
      String template = """ 
          {"task":"text_embedding", "model":"%s", "text":"%s"}
          """;
      request = String.format(template, model, text);
    }
    return parseEmbedding(client.postJsonString(request));
  }


  private float[] embedImage(BufferedImage image) throws IOException, InterruptedException {
    String request;
    if (model == null){
      String template = """
          {"task":"image_embedding", "image":"%s"}
          """;
      request = String.format(template, ImageParser.bufferedImageToDataURL(image, "png"));
    }else{
      String template = """
          {"task":"image_embedding", "model":"%s", "image":"%s"}
          """;
          request = String.format(template, model, ImageParser.bufferedImageToDataURL(image, "png"));
    }
    return parseEmbedding(client.postJsonString(request));

  }

  private float[] parseEmbedding(String json) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, float[].class);
  }


  private List<float[]> parseEmbeddingList(String json) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    // Parse JSON to List<List<Double>>
    List<List<Double>> lists = objectMapper.readValue(json, new TypeReference<List<List<Double>>>() {});

    // Convert List<List<Double>> to List<float[]>
    List<float[]> result = new ArrayList<>();
    for (List<Double> list : lists) {
      float[] arr = new float[list.size()];
      for (int i = 0; i < list.size(); i++) {
        arr[i] = list.get(i).floatValue();
      }
      result.add(arr);
    }

    return result;
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
  private float[] embedMostRepresentativeImages(List<BufferedImage> images, ViewpointStrategy viewpointStrategy) throws IOException, InterruptedException {

    var retVal = new float[embedding_size];

    switch (viewpointStrategy) {
      // Strategy for embedding multiple images. Choose mean of the most contained cluster. Project mean to unit hypersphere.
      case MULTI_IMAGE_KMEANS -> {
        var floatvectors = new ArrayList<FloatVectorImpl>();
        var vectors = embedMultipleImages(images);
        vectors.forEach(v -> floatvectors.add(new FloatVectorImpl(v)));
        var kmeans = KMeansPP.bestOfkMeansPP(floatvectors, new FloatVectorImpl(new float[embedding_size]), 3, -1f, 5);
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
        var vectorsMean = new float[embedding_size];
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
  private List<float[]> embedMultipleImages(List<BufferedImage> images) throws IOException, InterruptedException {
    String request = "{\"task\":\"image_embedding\", ";
    if (model!=null){
      request += "\"model\":\"" + model + "\", ";
    }
    request += "\"image\":[";
    if (!images.isEmpty()) {
      for (var image : images) {
        request += "\"" + ImageParser.bufferedImageToDataURL(image, "png") + "\",";
      }
      request = request.substring(0, request.length() - 1); // Remove trailing comma
    }
    request += "]";
    request += "}";
    String json = client.postJsonString(request);
    return parseEmbeddingList(json);

  }

  /**
   * This method embeds a 3D model and returns the feature vector.
   */
  private float[] embedModel(IModel model) throws IOException, InterruptedException {
    //Options for window
    var windowOptions = new WindowOptions() {{
      this.hideWindow = true;
      this.width = 600;
      this.height = 600;
    }};
    // Options for renderer
    var renderOptions = new RenderOptions() {{
      this.showTextures = true;
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
      return new float[embedding_size];
    }
    if (images.size() == 1) {
      return embedImage(images.get(0));
    }
    return embedMostRepresentativeImages(images, viewpointStrategy);
  }


  private float[] embedVideo(List<MultiImage> frames) throws IOException, InterruptedException {
    var images = frames.stream().map(image -> image.getBufferedImage()).collect(Collectors.toList());
    images = images.stream().map(img -> new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB)).collect(Collectors.toList());
    var encodings = embedMultipleImages(images);

    // Sum
    float[] meanEncoding = encodings.stream().reduce(new float[embedding_size], (encoding0, encoding1) -> {
      float[] tempSum = new float[embedding_size];

      for (int i = 0; i < embedding_size; i++) {
        tempSum[i] = encoding0[i] + encoding1[i];
      }

      return tempSum;
    });

    // Calculate mean
    for (int i = 0; i < embedding_size; i++) {
      meanEncoding[i] /= encodings.size();
    }

    return meanEncoding;
  }
}
