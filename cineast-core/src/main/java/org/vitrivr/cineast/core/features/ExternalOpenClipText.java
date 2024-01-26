package org.vitrivr.cineast.core.features;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;

public class ExternalOpenClipText implements Retriever {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final String TABLE_NAME = "features_openclip";

  private static final int EMBEDDING_SIZE = 512;
  private static final ReadableQueryConfig.Distance DISTANCE = ReadableQueryConfig.Distance.cosine;
  private static final CorrespondenceFunction CORRESPONDENCE = CorrespondenceFunction.linear(1);

  private static final String DEFAULT_API_ENDPOINT = "http://localhost:8888";

  private static final String API_ENDPOINT_KEY = "api";

  private final String externalApi;

  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .build();

  private final JsonMapper mapper = new JsonMapper();
  private DBSelector selector;


  public ExternalOpenClipText() {
    this.externalApi = DEFAULT_API_ENDPOINT;
  }

  public ExternalOpenClipText(Map<String,String> properties){
    this.externalApi = properties.getOrDefault(API_ENDPOINT_KEY, DEFAULT_API_ENDPOINT);
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity(TABLE_NAME, true, EMBEDDING_SIZE);
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().dropEntity(TABLE_NAME);
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(TABLE_NAME);
  }

  private float[] apiRequest(String query) throws IOException, InterruptedException {

    String builder = URLEncoder.encode("query", StandardCharsets.UTF_8)
        + "="
        + URLEncoder.encode(query, StandardCharsets.UTF_8);

    HttpRequest request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(builder))
        .uri(URI.create(externalApi))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IllegalStateException("received response code " + response.statusCode());
    }

    return mapper.readValue(response.body(), float[].class);

  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    String text = sc.getText();
    if (text == null || text.isBlank()) {
      return Collections.emptyList();
    }

    try {
      float[] arr = apiRequest(text);
      return getSimilar(new FloatArrayTypeProvider(arr), qc);
    } catch (Exception e) {
      LOGGER.error("error during CLIPText execution", e);
      return new ArrayList<>();
    }
  }

  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    List<PrimitiveTypeProvider> list = this.selector.getFeatureVectorsGeneric(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId), FEATURE_COLUMN_QUALIFIER, qc);
    if (list.isEmpty()) {
      LOGGER.warn("No feature vector for shotId {} found, returning empty result-list", segmentId);
      return Collections.emptyList();
    }
    return getSimilar(list.get(0), qc);
  }

  private List<ScoreElement> getSimilar(PrimitiveTypeProvider queryProvider, ReadableQueryConfig qc) {
    ReadableQueryConfig qcc = QueryConfig.clone(qc).setDistance(DISTANCE);
    final List<SegmentDistanceElement> distances = this.selector.getNearestNeighboursGeneric(qc.getResultsPerModule(), queryProvider, FEATURE_COLUMN_QUALIFIER, SegmentDistanceElement.class, qcc);
    final CorrespondenceFunction function = qcc.getCorrespondenceFunction().orElse(
      CorrespondenceFunction.linear(distances.stream().map(SegmentDistanceElement::getDistance).max(Double::compare).orElse(1d))
    );
    return DistanceElement.toScore(distances, function);
  }

  @Override
  public void finish() {
    if (this.selector != null) {
      this.selector.close();
      this.selector = null;
    }
  }
}
