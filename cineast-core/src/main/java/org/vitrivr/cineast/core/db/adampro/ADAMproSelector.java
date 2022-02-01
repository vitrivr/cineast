package org.vitrivr.cineast.core.db.adampro;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExternalHandlerQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.SubExpressionQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.VectorMessage;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.util.LogHelper;

public class ADAMproSelector extends AbstractADAMproSelector {


  private static final Logger LOGGER = LogManager.getLogger();

  public ADAMproSelector(ADAMproWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, this.mb.buildBooleanQueryMessage(this.mb.buildWhereMessage(fieldName, value.getString())), null, null);

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

  /**
   * Performs a batched kNN-search with multiple vectors. That is, ADAM pro is tasked to perform the kNN search for each vector in the provided list and return results of each query.
   *
   * @param k                    The number k vectors to return per query.
   * @param vectors              The list of vectors to use.
   * @param column               The column to perform the kNN search on.
   * @param distanceElementClass The class to use to create the resulting DistanceElements
   * @param configs              The query configurations, which may contain distance definitions or query-hints. Every feature should have its own QueryConfig object.
   * @param <T>                  The type T of the resulting DistanceElements.
   * @return List of results.
   */
  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs) {
    /* Check if sizes of configs and vectors array correspond. */
    if (vectors.size() > configs.size()) {
      throw new IllegalArgumentException("You must provide a separate QueryConfig entry for each vector - even if it is the same instance of the QueryConfig.");
    }

    /* Prepare list of QueryMessages. */
    List<QueryMessage> queryMessages = new ArrayList<>(vectors.size());
    for (int i = 0; i < vectors.size(); i++) {
      float[] vector = vectors.get(i);
      ReadableQueryConfig config = configs.get(i);

      /* Extract hints from QueryConfig. If they're not set, then replace by DEFAULT_HINT. */
      Collection<ReadableQueryConfig.Hints> hints;
      if (!config.getHints().isEmpty()) {
        hints = config.getHints();
      } else {
        hints = ADAMproMessageBuilder.DEFAULT_HINT;
      }

      NearestNeighbourQueryMessage nnqMessage = this.mb.buildNearestNeighbourQueryMessage(column, DataMessageConverter.convertVectorMessage(vector), k, config);
      queryMessages.add(this.mb.buildQueryMessage(hints, this.fromMessage, this.mb.inList("id", config.getRelevantSegmentIds()), ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage));
    }

    /* Prepare a BatchedQueryMessage. */
    BatchedQueryMessage batchedQueryMessage = this.mb.buildBatchedQueryMessage(queryMessages);

    ListenableFuture<BatchedQueryResultsMessage> future = this.adampro.batchedQuery(batchedQueryMessage);

    BatchedQueryResultsMessage result;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      return new ArrayList<>(0);
    }

    /* Prepare empty list of results. */
    List<T> results = new ArrayList<>(result.getResultsCount());

    /*
     * Merge results of the partial queries.
     */
    for (int i = 0; i < result.getResultsCount(); i++) {
      QueryResultsMessage partial = result.getResults(i);
      AckMessage ack = partial.getAck();
      if (ack.getCode() != AckMessage.Code.OK) {
        LOGGER.error("error in getNearestNeighbours on entity {}, ({}) : {}", entityName, ack.getCode(), ack.getMessage());
        continue;
      }

      if (partial.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = partial.getResponses(0); // only head (end-result) is important
      results.addAll(handleNearestNeighbourResponse(response, k, distanceElementClass));
    }

    return results;
  }

  @Override
  public <T extends DistanceElement> List<T> getNearestNeighboursGeneric(int k, float[] vector, String column,
      Class<T> distanceElementClass, ReadableQueryConfig config) {
    NearestNeighbourQueryMessage nnqMessage = mb.buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);
    QueryMessage sqMessage = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fromMessage, this.mb.inList("id", config.getRelevantSegmentIds()), ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);
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
      LOGGER.error("Query was {} ", sqMessage.toString());
      return new ArrayList<>(0);
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important
    return handleNearestNeighbourResponse(response, k, distanceElementClass);
  }


  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    if (values == null || Iterables.isEmpty(values)) {
      return new ArrayList<>(0);
    }

    /* TODO: Escape quotes. */
    final WhereMessage where = this.mb.buildWhereMessage(fieldName, StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::getString).collect(Collectors.toList()), operator);
    final BooleanQueryMessage bqMessage = this.mb.buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
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
      LOGGER.error("Query returned non-OK result code {} with message: {}",
          result.getAck().getCode(),
          result.getAck().getMessage());
    }

    if (result.getResponsesCount() == 0) {
      return new ArrayList<>(0);
    }

    QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important

    List<QueryResultTupleMessage> resultList = response.getResultsList();
    return resultsToMap(resultList);
  }

  private List<Map<String, PrimitiveTypeProvider>> executeBooleanQuery(BooleanQueryMessage bqm) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, bqm, null, null);
    return executeQuery(qbqm);
  }


  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
    NearestNeighbourQueryMessage nnqMessage = this.mb.buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);

    /* Extract hints from QueryConfig. If they're not set, then replace by DEFAULT_HINT. */
    Collection<ReadableQueryConfig.Hints> hints;
    if (!config.getHints().isEmpty()) {
      hints = config.getHints();
    } else {
      hints = ADAMproMessageBuilder.DEFAULT_HINT;
    }

    QueryMessage sqMessage = this.mb.buildQueryMessage(hints, this.fromMessage, this.mb.inList("id", config.getRelevantSegmentIds()), null, nnqMessage);

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
      LOGGER.error("error in getNearestNeighbourRows, entity {} ({}) : {}", entityName, ack.getCode(), ack.getMessage());
      return _return;
    }
    return resultsToMap(response.getResultsList());
  }

  /**
   * Performs a fulltext search with multiple query terms. The underlying entity is expected to use Apache Solr as storage handler. If it doesn't, this method will fail!
   * <p>
   * The method performs an Apache Solr lookup equivalent to: [field]: ([term1] [term2] ... [termN]). Full Lucene query syntax is supported.
   * <p>
   * TODO: This is a quick & dirty solution. Should be re-engineered to fit different use-cases.
   *
   * @param rows      The number of rows that should be returned.
   * @param fieldname The field that should be used for lookup.
   * @param terms     The query terms. Individual terms will be connected by a logical OR.
   * @return List of rows that math the fulltext search.
   */
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
    final ExternalHandlerQueryMessage.Builder ehqmBuilder = ExternalHandlerQueryMessage.newBuilder();
    ehqmBuilder.setEntity(this.entityName);
    ehqmBuilder.setHandler("solr");

    /* */
    final Map<String, String> parameters = new HashMap<>();
    parameters.put("rows", Integer.toString(rows));

    final StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (String item : terms) {
      sb.append(item);
      sb.append(" ");
    }
    sb.append(')');
    parameters.put("query", fieldname + ":" + sb.toString());

    ehqmBuilder.putAllParams(parameters);

    SubExpressionQueryMessage.Builder seqmBuilder = SubExpressionQueryMessage.newBuilder();
    seqmBuilder.setEhqm(ehqmBuilder);

    FromMessage.Builder fmBuilder = FromMessage.newBuilder();
    fmBuilder.setExpression(seqmBuilder);

    QueryMessage qm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fmBuilder, this.mb.inList("id", queryConfig.getRelevantSegmentIds()), null, null);

    return executeQuery(qm);
  }
}
