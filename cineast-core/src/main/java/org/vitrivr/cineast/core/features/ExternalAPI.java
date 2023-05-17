package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.ENTITY_NAME_KEY;
import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.CorrespondenceFunctionEnum;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.entities.SimplePrimitiveTypeProviderFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.PrimitiveTypeProviderFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.web.ImageParser;
import com.google.gson.Gson;
import java.io.File;
import javax.imageio.ImageIO;

public class ExternalAPI implements Extractor, Retriever {

  /**
   * TODO only for developing purposes
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    var properties = new HashMap<String, String>();
    properties.put(ENTITY_NAME_KEY, "dev-feature");
    properties.put(FEATURE_KEY, "TEXT");
    properties.put(MODEL_KEY, "clipcap");
    properties.put(ENDPOINT_KEY, "http://localhost:5000");
    var extractor = new ExternalAPI(properties);
    var file = new File("../PTT/objects/PTT-Archiv/Poststellenchroniken/Post-199_A_0003_Affoltern_im_Emmental/Post-199_A_0003_Affoltern_im_Emmental_001.png");
    BufferedImage img = ImageIO.read(new File("../PTT/objects/PTT-Archiv/Poststellenchroniken/Post-199_A_0003_Affoltern_im_Emmental/Post-199_A_0003_Affoltern_im_Emmental_001.png")); //google how to load a file into this  PTT/objects/PTT-Archiv/Poststellenchroniken/Post-199_A_0003_Affoltern_im_Emmental/Post-199_A_0003_Affoltern_im_Emmental_001.png
    var feature = extractor.extractImageFeature(img);
    System.out.println(feature);
  }

  private static final Logger LOGGER = LogManager.getLogger();

  private static final String FEATURE_KEY = "featuretype";
  private static final String MODEL_KEY = "model";
  private static final String ENDPOINT_KEY = "endpoint";
  private static final String DISTANCE_KEY = "distance";
  private static final String CORRESPONDENCE_FUN_KEY = "correspondence";
  private static final String CORRESPONDENCE_PARAM_1_KEY = "correspondence_p1";
  private static final String VECTOR_LEN_KEY = "vectorlen";

  private String entityName;
  private FeatureType featureType;
  private String model;
  private String endpoint;
  private Distance distance;
  private CorrespondenceFunction correspondence;
  private int vectorLen;

  protected SimpleFeatureDescriptorWriter writer;
  protected PrimitiveTypeProviderFeatureDescriptorWriter primitiveWriter;
  protected DBSelector selector;
  protected PersistencyWriter<?> phandler;
  private final JsonMapper mapper = new JsonMapper();

  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .build();

  /**
   * Usage without parameters is also not ok for retrieval, we still need a entity name
   */
  protected ExternalAPI() {
    this(new HashMap<>());
  }

  protected ExternalAPI(Map<String, String> properties) {
    if (!properties.containsKey(ENTITY_NAME_KEY) || !properties.containsKey(FEATURE_KEY) || !properties.containsKey(MODEL_KEY)) {
      throw new RuntimeException(ENTITY_NAME_KEY + " " + FEATURE_KEY + " " + MODEL_KEY + " are mandatory properties");
    }
    this.entityName = properties.get(ENTITY_NAME_KEY);
    this.featureType = FeatureType.valueOf(properties.get(FEATURE_KEY));
    this.model = properties.get(MODEL_KEY);
    // endpoint can be null if the feature only relies on an external API during extraction but not during retrieval
    this.endpoint = properties.getOrDefault(ENDPOINT_KEY, null);
    if (properties.containsKey(DISTANCE_KEY)) {
      this.distance = Distance.valueOf(properties.get(DISTANCE_KEY));
    }
    if (properties.containsKey(CORRESPONDENCE_FUN_KEY)) {
      String param = properties.getOrDefault(CORRESPONDENCE_PARAM_1_KEY, null);
      switch (CorrespondenceFunctionEnum.valueOf(properties.get(CORRESPONDENCE_FUN_KEY))) {
        case IDENTITY -> {
          if (param == null) {
            this.correspondence = CorrespondenceFunction.identity();
            break;
          }
          this.correspondence = CorrespondenceFunction.identityMultiple(Double.parseDouble(param));
        }
        case LINEAR -> {
          if (param == null) {
            throw new IllegalStateException("no param provided for linear correspondence function");
          }
          this.correspondence = CorrespondenceFunction.linear(Double.parseDouble(param));
        }
        case HYPERBOLIC -> {
          if (param == null) {
            throw new IllegalStateException("no param provided for hyperbolic correspondence function");
          }
          this.correspondence = CorrespondenceFunction.hyperbolic(Double.parseDouble(param));
        }
      }
    }
    if (this.featureType == FeatureType.VECTOR && properties.containsKey(VECTOR_LEN_KEY)) {
      this.vectorLen = Integer.parseInt(properties.getOrDefault(VECTOR_LEN_KEY, null));
    }
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(entityName, true, vectorLen);
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(entityName);
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) {
    this.phandler = phandlerSupply.get();
    this.writer = new SimpleFeatureDescriptorWriter(this.phandler, this.entityName);
    this.primitiveWriter = new PrimitiveTypeProviderFeatureDescriptorWriter(this.phandler, this.entityName);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (phandler.idExists(shot.getId())) {
      return;
    }

    try {
      var bufImg = shot.getMostRepresentativeFrame().getImage().getBufferedImage();

      var features = extractImageFeature(bufImg).stream().map(e -> new SimplePrimitiveTypeProviderFeatureDescriptor(shot.getId(), e)).toList();

      this.primitiveWriter.write(features);
    } catch (Exception e) {
      LOGGER.error("Error during extraction", e);
    }
  }

  List<PrimitiveTypeProvider> extractImageFeature(BufferedImage bufImg) throws IOException, InterruptedException {
    var query = ImageParser.bufferedImageToDataURL(bufImg, "png");
    // TODO build
    Gson gson = new Gson();

    Map<String, Object> map = new HashMap<>();
    map.put("image", query);

    String body = gson.toJson(map);

    return performPost(body);
  }


  List<PrimitiveTypeProvider> extractTextFeature(String txt) throws IOException, InterruptedException {
    // TODO build
    String body = "";

    return performPost(body);
  }

  private List<PrimitiveTypeProvider> performPost(String body) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .uri(URI.create(endpoint + "/extract/"))
        .header("Content-Type", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("received response code " + response.statusCode());
    }

    switch (featureType) {
      case TEXT -> {
        return Arrays.stream(mapper.readValue(response.body(), String[].class)).map(StringTypeProvider::new).collect(Collectors.toList());
      }
      case VECTOR -> {
        return Arrays.stream(mapper.readValue(response.body(), float[][].class)).map(FloatArrayTypeProvider::new).collect(Collectors.toList());
      }
      default -> throw new IllegalStateException("Unexpected value: " + featureType);
    }
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(entityName);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    try {
      ReadableQueryConfig qcc = setQueryConfig(qc);
      if (!sc.getText().isEmpty()) {
        // there is not really a case where one string returns multiple vectors in our scenario
        var feature = extractTextFeature(sc.getText()).get(0);
        return getSimilar(feature, qcc);
      }
      // default = image
      PrimitiveTypeProvider feature = null;
      // possibly handle cases more gracefully where multiple vectors are generated
      feature = extractImageFeature(sc.getMostRepresentativeFrame().getImage().getBufferedImage()).get(0);
      return getSimilar(feature, qcc);
    } catch (Exception e) {
      LOGGER.error("Error during retrieval", e);
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    ReadableQueryConfig qcc = setQueryConfig(qc);
    List<PrimitiveTypeProvider> list = this.selector.getFeatures(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER, qc);
    if (list.isEmpty()) {
      LOGGER.warn("No feature vector for shotId {} found, returning empty result-list", segmentId);
      return Collections.emptyList();
    }
    return getSimilar(list.get(0), qcc);
  }

  protected List<ScoreElement> getSimilar(PrimitiveTypeProvider queryProvider, ReadableQueryConfig qc) {
    List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qc.getResultsPerModule(), queryProvider, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, qc);
    CorrespondenceFunction function = qc.getCorrespondenceFunction().orElse(correspondence);
    return DistanceElement.toScore(distances, function);
  }

  protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return new QueryConfig(qc).setCorrespondenceFunctionIfEmpty(this.correspondence).setDistanceIfEmpty(this.distance);
  }

  @Override
  public void finish() {
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }

    if (this.primitiveWriter != null) {
      this.primitiveWriter.close();
      this.primitiveWriter = null;
    }

    if (this.phandler != null) {
      this.phandler.close();
      this.phandler = null;
    }

    if (this.selector != null) {
      this.selector.close();
      this.selector = null;
    }
  }


  @Override
  public List<String> getEntityNames() {
    return Lists.newArrayList(entityName);
  }

  private enum FeatureType {
    TEXT, VECTOR
  }

}
