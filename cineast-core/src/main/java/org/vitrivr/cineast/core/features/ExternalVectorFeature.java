package org.vitrivr.cineast.core.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class ExternalVectorFeature extends AbstractFeatureModule {

  public static final String DEFAULT_TABLE_NAME = "features_externalVector";
  public static final float DEFAULT_MAX_DIST = 2.0f;
  public static final int DEFAULT_EMBEDDING_SIZE = 512;

  public static final Distance DEFAULT_DISTANCE = Distance.euclidean;

  public final int embedding_size;

  public final Distance distance;


  private static final Logger LOGGER = LogManager.getLogger();

  final FeatureClient textClient;
  final FeatureClient imageClient;

  public ExternalVectorFeature() {
    super(DEFAULT_TABLE_NAME, DEFAULT_MAX_DIST, DEFAULT_EMBEDDING_SIZE);
    embedding_size = DEFAULT_EMBEDDING_SIZE;
    distance = DEFAULT_DISTANCE;
    throw new IllegalArgumentException("no properties specified");
  }

  public ExternalVectorFeature(Map<String, String> properties) throws IOException {
    super(properties.getOrDefault(
        CineastConstants.ENTITY_NAME_KEY, DEFAULT_TABLE_NAME),
        Float.parseFloat(properties.getOrDefault("max_dist", String.valueOf(DEFAULT_MAX_DIST))),
        Integer.parseInt(properties.getOrDefault("embedding_size", String.valueOf(DEFAULT_EMBEDDING_SIZE)))
    );
    embedding_size = Integer.parseInt(properties.getOrDefault("embedding_size", String.valueOf(DEFAULT_EMBEDDING_SIZE)));
    distance = Distance.valueOf(properties.getOrDefault("distance", DEFAULT_DISTANCE.name()));
    if(!properties.containsKey("textEndpoint")){
      throw new IllegalArgumentException("No endpoint specified for extracting text with external client");
    }
    String textEndpoint = properties.get("textEndpoint");
    if(!properties.containsKey("textRequest")){
      throw new IllegalArgumentException("No request specified for extracting text with external client");
    }
    String textRequest = properties.get("textRequest");
    if(!properties.containsKey("textResponse")){
      throw new IllegalArgumentException("No response template specified for extracting text with external client");
    }
    if(!properties.containsKey("imageEndpoint")){
      throw new IllegalArgumentException("No endpoint specified for extracting image with external client");
    }
    String imageEndpoint = properties.get("imageEndpoint");
    if(!properties.containsKey("imageRequest")){
      throw new IllegalArgumentException("No request specified for extracting image with external client");
    }
    String imageRequest = properties.get("imageRequest");
    if(!properties.containsKey("imageResponse")){
      throw new IllegalArgumentException("No response template specified for extracting image with external client");
    }
    textClient = FeatureClient.build(Map.of("endpoint", textEndpoint, "request", textRequest, "response", properties.get("textResponse")));
    imageClient = FeatureClient.build(Map.of("endpoint", imageEndpoint, "request", imageRequest, "response", properties.get("imageResponse")));

  }
  @Override
  public void processSegment(SegmentContainer sc) {
    // Return if already processed
    if (phandler.idExists(sc.getId())) {
      return;
    }

    // Case: segment contains image
    if (sc.getMostRepresentativeFrame() != VideoFrame.EMPTY_VIDEO_FRAME) {
      BufferedImage image = sc.getMostRepresentativeFrame().getImage().getBufferedImage();

      if (image != null) {
        float[] embeddingArray = new float[0];
        try {
          embeddingArray = (float[]) imageClient.extract(sc).getOrDefault("feature", "");
        } catch (Exception e) {
          LOGGER.error("Error during extraction", e);
        }
        this.persist(sc.getId(), new FloatVectorImpl(embeddingArray));
      }

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
        Map<String,Object> extractions = textClient.extract(sc);
        embeddingArray = (float[]) extractions.getOrDefault("feature", "");
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
          embeddingArray = (float[]) imageClient.extract(sc).getOrDefault("feature", "");
        } catch (Exception e) {
          LOGGER.error("Error during extraction", e);
        }
        return getSimilar(embeddingArray, queryConfig);
      }

      LOGGER.error("Image was provided, but could not be decoded!");
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

}
