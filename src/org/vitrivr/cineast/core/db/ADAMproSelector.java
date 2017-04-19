package org.vitrivr.cineast.core.db;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DistanceMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DistanceMessage.DistanceType;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExternalHandlerQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ProjectionMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.SubExpressionQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.VectorMessage;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.DefaultValueHashMap;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.DistanceElement;
import org.vitrivr.cineast.core.util.LogHelper;

public class ADAMproSelector implements DBSelector {

  /**
   * flag to choose if every selector should have its own connection to ADAMpro or if they should
   * share one.
   */
  private static boolean useGlobalWrapper = true;

  private static final ADAMproWrapper GLOBAL_ADAMPRO_WRAPPER = useGlobalWrapper
      ? new ADAMproWrapper() : null;

  private ADAMproWrapper adampro = useGlobalWrapper ? GLOBAL_ADAMPRO_WRAPPER : new ADAMproWrapper();

  private FromMessage.Builder fromBuilder = FromMessage.newBuilder();
  private final QueryMessage.Builder qmBuilder = QueryMessage.newBuilder();
  private final NearestNeighbourQueryMessage.Builder nnqmBuilder = NearestNeighbourQueryMessage
      .newBuilder();
  private final BooleanQueryMessage.Builder bqmBuilder = BooleanQueryMessage.newBuilder();
  private final WhereMessage.Builder wmBuilder = WhereMessage.newBuilder();
  private final DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();
  private static final Logger LOGGER = LogManager.getLogger();

  private static ArrayList<String> hints = new ArrayList<>(1);
  private static ProjectionMessage projectionMessage;

  private String entityName;

  private static final DistanceMessage chisquared, correlation, cosine, hamming, jaccard,
      kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, spannorm, haversine;

  static {

    hints.add("exact");

    projectionMessage = AdamGrpc.ProjectionMessage.newBuilder()
        .setAttributes(AdamGrpc.ProjectionMessage.AttributeNameMessage.newBuilder()
            .addAttribute("ap_distance").addAttribute("id"))
        .build();

    DistanceMessage.Builder dmBuilder = DistanceMessage.newBuilder();

    chisquared = dmBuilder.clear().setDistancetype(DistanceType.chisquared).build();
    correlation = dmBuilder.clear().setDistancetype(DistanceType.correlation).build();
    cosine = dmBuilder.clear().setDistancetype(DistanceType.cosine).build();
    hamming = dmBuilder.clear().setDistancetype(DistanceType.hamming).build();
    jaccard = dmBuilder.clear().setDistancetype(DistanceType.jaccard).build();
    kullbackleibler = dmBuilder.clear().setDistancetype(DistanceType.kullbackleibler).build();
    chebyshev = dmBuilder.clear().setDistancetype(DistanceType.chebyshev).build();
    euclidean = dmBuilder.clear().setDistancetype(DistanceType.euclidean).build();
    squaredeuclidean = dmBuilder.clear().setDistancetype(DistanceType.squaredeuclidean).build();
    manhattan = dmBuilder.clear().setDistancetype(DistanceType.manhattan).build();
    spannorm = dmBuilder.clear().setDistancetype(DistanceType.spannorm).build();
    haversine = dmBuilder.clear().setDistancetype(DistanceType.haversine).build();

  }

  @Override
  public boolean open(String name) {
    this.fromBuilder.setEntity(name);
    this.entityName = name;
    return true;
  }

  @Override
  public boolean close() {
    if (useGlobalWrapper) {
      return false;
    }
    this.adampro.close();
    return true;
  }

  private QueryMessage buildQueryMessage(ArrayList<String> hints, FromMessage.Builder fb,
      BooleanQueryMessage bqMessage, ProjectionMessage pMessage,
      NearestNeighbourQueryMessage nnqMessage) {
    synchronized (qmBuilder) {
      qmBuilder.clear();
      qmBuilder.setFrom(fb);
      if (hints != null && !hints.isEmpty()) {
        qmBuilder.addAllHints(hints);
      }
      if (bqMessage != null) {
        qmBuilder.setBq(bqMessage);
      }
      if (pMessage != null) {
        qmBuilder.setProjection(pMessage);
      }
      if (nnqMessage != null) {
        qmBuilder.setNnq(nnqMessage);
      }

      return qmBuilder.build();
    }
  }

  private QueryMessage buildQueryMessage(ArrayList<String> hints, BooleanQueryMessage bqMessage,
      ProjectionMessage pMessage, NearestNeighbourQueryMessage nnqMessage) {
    return buildQueryMessage(hints, fromBuilder, bqMessage, pMessage, nnqMessage);
  }

  private BooleanQueryMessage buildBooleanQueryMessage(WhereMessage where,
      WhereMessage... whereMessages) {
    ArrayList<WhereMessage> tmp = new ArrayList<>(
        1 + (whereMessages == null ? 0 : whereMessages.length));
    tmp.add(where);
    if (whereMessages != null) {
      Collections.addAll(tmp, whereMessages);
    }
    synchronized (bqmBuilder) {
      bqmBuilder.clear();
      return bqmBuilder.addAllWhere(tmp).build();
    }
  }

  private WhereMessage buildWhereMessage(String key, String value) {
    synchronized (wmBuilder) {
      wmBuilder.clear();
      return wmBuilder.setAttribute(key).addValues(DataMessage.newBuilder().setStringData(value))
          .build();
    }
  }

  private WhereMessage buildWhereMessage(String key, String... values) {
    synchronized (wmBuilder) {
      wmBuilder.clear();
      DataMessage.Builder damBuilder = DataMessage.newBuilder();

      wmBuilder.setAttribute(key);

      for (String value : values) {
        wmBuilder.addValues(damBuilder.setStringData(value).build());
      }

      return wmBuilder.build();
    }
  }

  private WhereMessage buildWhereMessage(String key, Iterable<String> values) {
    synchronized (wmBuilder) {
      wmBuilder.clear();
      DataMessage.Builder damBuilder = DataMessage.newBuilder();

      wmBuilder.setAttribute(key);

      for (String value : values) {
        wmBuilder.addValues(damBuilder.setStringData(value).build());
      }

      return wmBuilder.build();
    }
  }

  private NearestNeighbourQueryMessage buildNearestNeighbourQueryMessage(String column,
      VectorMessage fvm, int k, ReadableQueryConfig qc) {
    synchronized (nnqmBuilder) {
      this.nnqmBuilder.clear();
      nnqmBuilder.setAttribute(column).setQuery(fvm).setK(k);
      nnqmBuilder.setDistance(buildDistanceMessage(qc));
      if (qc != null) {
        Optional<float[]> weights = qc.getDistanceWeights();
        if (weights.isPresent()) {
          nnqmBuilder.setWeights(DataMessageConverter.convertVectorMessage(weights.get()));
        }
      }
      return nnqmBuilder.build();
    }
  }

  private DistanceMessage buildDistanceMessage(ReadableQueryConfig qc) {
    if (qc == null) {
      return manhattan;
    }
    Optional<Distance> distance = qc.getDistance();
    if (!distance.isPresent()) {
      return manhattan;
    }
    switch (distance.get()) {
      case chebyshev:
        return chebyshev;
      case chisquared:
        return chisquared;
      case correlation:
        return correlation;
      case cosine:
        return cosine;
      case euclidean:
        return euclidean;
      case hamming:
        return hamming;
      case jaccard:
        return jaccard;
      case kullbackleibler:
        return kullbackleibler;
      case manhattan:
        return manhattan;
      case minkowski: {

        float norm = qc.getNorm().orElse(1f);

        if (Math.abs(norm - 1f) < 1e6f) {
          return manhattan;
        }

        if (Math.abs(norm - 2f) < 1e6f) {
          return euclidean;
        }

        HashMap<String, String> tmp = new HashMap<>();
        tmp.put("norm", Float.toString(norm));

        synchronized (dmBuilder) {
          return dmBuilder.clear().setDistancetype(DistanceType.minkowski).putAllOptions(tmp)
              .build();
        }

      }
      case spannorm:
        return spannorm;
      case squaredeuclidean:
        return squaredeuclidean;
      case haversine:
        return haversine;
      default:
        return manhattan;
    }
  }

  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    QueryMessage qbqm = buildQueryMessage(hints,
        buildBooleanQueryMessage(buildWhereMessage(fieldName, value)), null, null);

    ListenableFuture<QueryResultsMessage> f = this.adampro.booleanQuery(qbqm);
    ArrayList<float[]> _return = new ArrayList<>();
    QueryResultsMessage r;
    try {
      r = f.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    if (r.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = r.getResponses(0); // only head (end-result) is important

    AckMessage ack = response.getAck();
    if (ack.getCode() != Code.OK) {
      LOGGER.error("error in getFeatureVectors on entity {}, ({}) : {}", entityName, ack.getCode(), ack.getMessage());
      return _return;
    }

    for (QueryResultTupleMessage result : response.getResultsList()) {

      Map<String, DataMessage> data = result.getDataMap();

      if (!data.containsKey(vectorName)) {
        continue;
      }

      DataMessage dm = data.get(vectorName);

      if (dm.getDatatypeCase() != DataMessage.DatatypeCase.VECTORDATA) {
        continue;
      }

      VectorMessage featureData = dm.getVectorData();

      if (featureData.getVectorCase() != VectorMessage.VectorCase.DENSEVECTOR) {
        continue; // TODO add correct handling for sparse and int vectors
      }

      DenseVectorMessage dense = featureData.getDenseVector();

      List<Float> list = dense.getVectorList();
      if (list.isEmpty()) {
        continue;
      }

      float[] vector = new float[list.size()];
      int i = 0;
      for (float x : list) {
        vector[i++] = x;
      }

      _return.add(vector);

    }

    return _return;

  }

  @Override
  public <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector, String column,
      Class<T> distanceElementClass, ReadableQueryConfig config) {
    NearestNeighbourQueryMessage nnqMessage = buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);
    QueryMessage sqMessage = buildQueryMessage(hints, null, projectionMessage, nnqMessage);
    ListenableFuture<QueryResultsMessage> future = this.adampro.standardQuery(sqMessage);

    QueryResultsMessage result;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    AckMessage ack = result.getAck();
    if (ack.getCode() != AckMessage.Code.OK) {
      LOGGER.error("error in getNearestNeighbours on entity {}, ({}) : {}", entityName, ack.getCode(), ack.getMessage());
      return new ArrayList<>(0);
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important
    return handleNearestNeighbourResponse(response, k, distanceElementClass, config);
  }

  private <T extends DistanceElement> List<T> handleNearestNeighbourResponse(QueryResultInfoMessage response,
      int k, Class<T> distanceElementClass, ReadableQueryConfig config) {
    List<T> result = new ArrayList<>(k);
    for (QueryResultTupleMessage msg : response.getResultsList()) {
      String id = msg.getDataMap().get("id").getStringData();
      if (id == null) {
        continue;
      }
      double distance = msg.getDataMap().get("ap_distance").getDoubleData();
      T e = DistanceElement.create(distanceElementClass, id, distance);
      if (e != null) {
        result.add(e);
      }
    }

    return result;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
    if (values == null || values.length == 0) {
      LOGGER.error("Cannot query empty value list in ADAMproSelector.getRows(), entity: {}", entityName);
      return new ArrayList<>(0);
    }

    if (values.length == 1) {
      return getRows(fieldName, values[0]);
    }

    WhereMessage where = buildWhereMessage(fieldName, values);
    BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      Iterable<String> values) {
    if (values == null) {
      LOGGER.error("Cannot query empty value list in ADAMproSelector.getRows(), entity: {}", entityName);
      return new ArrayList<>(0);
    }

    WhereMessage where = buildWhereMessage(fieldName, values);
    BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
    WhereMessage where = buildWhereMessage(fieldName, value);
    BooleanQueryMessage bqMessage = buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
  }

  /**
   * SELECT label FROM ... Be careful with the size of the resulting List :)
   */
  @Override
  public List<PrimitiveTypeProvider> getAll(String label) {
    List<Map<String, PrimitiveTypeProvider>> resultList = getAll();
    return resultList.stream().map(row -> row.get(label)).collect(Collectors.toList());
  }

  /**
   * TODO This is currently an ugly hack where we abuse the preview-function with LIMIT = COUNT()
   * using the getProperties ADAMpro-method. Once ADAMpro supports SELECT * FROM $ENTITY, this
   * method should be rewritten
   */
  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    ListenableFuture<PropertiesMessage> future = this.adampro.getProperties(
        EntityPropertiesMessage.newBuilder().setEntity(fromBuilder.getEntity()).build());
    int count = 1_000;
    PropertiesMessage propertiesMessage;
    try {
      propertiesMessage = future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in getAll, entitiy {}: {}", entityName, LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }
    try {
      count = Integer.parseInt(propertiesMessage.getPropertiesMap().get("count"));
    } catch (Exception e) {
      LOGGER.error("error in getAll, entitiy {}: {}", entityName, LogHelper.getStackTrace(e));
    }
    return preview(count);
  }

  @Override
  public boolean existsEntity(String eName) {
    ListenableFuture<ExistsMessage> future = this.adampro.existsEntity(eName);
    try {
      return future.get().getExists();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in existsEntity, entitiy {}: {}", entityName, LogHelper.getStackTrace(e));
      return false;
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
    PreviewMessage msg = PreviewMessage.newBuilder().setEntity(this.fromBuilder.getEntity()).setN(k)
        .build();
    ListenableFuture<QueryResultsMessage> f = this.adampro.previewEntity(msg);
    QueryResultsMessage result;
    try {
      result = f.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important

    List<QueryResultTupleMessage> resultList = response.getResultsList();
    return resultsToMap(resultList);
  }

  /**
   * @param resultList can be empty
   * @return an ArrayList of length one if the resultList is empty, else the transformed QueryResultTupleMessage
   */
  private List<Map<String, PrimitiveTypeProvider>> resultsToMap(
      List<QueryResultTupleMessage> resultList) {
    if (resultList.isEmpty()) {
      return new ArrayList<>(0);
    }

    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(resultList.size());

    for (QueryResultTupleMessage resultMessage : resultList) {
      Map<String, DataMessage> data = resultMessage.getDataMap();
      Set<String> keys = data.keySet();
      DefaultValueHashMap<String, PrimitiveTypeProvider> map = new DefaultValueHashMap<>(NothingProvider.INSTANCE);
      for (String key : keys) {
        map.put(key, DataMessageConverter.convert(data.get(key)));
      }
      _return.add(map);
    }

    return _return;
  }

  /**
   * Executes a QueryMessage and returns the resulting tuples
   *
   * @return an empty ArrayList if an error happens. Else just the list of rows
   */
  private List<Map<String, PrimitiveTypeProvider>> executeQuery(QueryMessage qm) {
    ListenableFuture<QueryResultsMessage> f = this.adampro.standardQuery(qm);
    QueryResultsMessage result;
    try {
      result = f.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    if (result.getAck().getCode() != AckMessage.Code.OK) {
      LOGGER.error(result.getAck().getMessage());
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important

    List<QueryResultTupleMessage> resultList = response.getResultsList();
    return resultsToMap(resultList);
  }

  private List<Map<String, PrimitiveTypeProvider>> executeBooleanQuery(BooleanQueryMessage bqm) {
    QueryMessage qbqm = buildQueryMessage(hints, bqm, null, null);
    return executeQuery(qbqm);
  }

  @Override
  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {
    NearestNeighbourQueryMessage nnqMessage = buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);

    QueryMessage sqMessage = buildQueryMessage(hints, null, null, nnqMessage);

    ListenableFuture<QueryResultsMessage> future = this.adampro.standardQuery(sqMessage);

    QueryResultsMessage result;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important

    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(k);

    AckMessage ack = response.getAck();
    if (ack.getCode() != Code.OK) {
      LOGGER.error("error in getNearestNeighbourRows, entitiy {} ({}) : {}", entityName, ack.getCode(), ack.getMessage());
      return _return;
    }
    return resultsToMap(response.getResultsList());
  }

  public List<Map<String, PrimitiveTypeProvider>> getFromExternal(String externalHandlerName,
      Map<String, String> parameters) {

    ExternalHandlerQueryMessage.Builder ehqmBuilder = ExternalHandlerQueryMessage.newBuilder();
    ehqmBuilder.setEntity(this.entityName);
    ehqmBuilder.setHandler(externalHandlerName);

    ehqmBuilder.putAllParams(parameters);

    SubExpressionQueryMessage.Builder seqmBuilder = SubExpressionQueryMessage.newBuilder();
    seqmBuilder.setEhqm(ehqmBuilder);

    FromMessage.Builder fmBuilder = FromMessage.newBuilder();
    fmBuilder.setExpression(seqmBuilder);

    QueryMessage qm = buildQueryMessage(hints, fmBuilder, null, null, null);

    return executeQuery(qm);
  }

}
