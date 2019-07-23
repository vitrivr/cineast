package org.vitrivr.cineast.core.db.adampro;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
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
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Hints;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.MergeOperation;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.util.LogHelper;

public class ADAMproStreamingSelector extends AbstractADAMproSelector {

  private static final Logger LOGGER = LogManager.getLogger();

  private List<Map<String, PrimitiveTypeProvider>> executeQuery(QueryMessage qm) {
    ArrayList<QueryResultsMessage> resultList = this.adampro
        .streamingStandardQuery(qm);

    if (resultList.isEmpty()){
      return Collections.emptyList();
    }

    List<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();

    for (QueryResultsMessage result : resultList) {

      if (result.getAck().getCode() != AckMessage.Code.OK) {
        LOGGER.error("Query returned non-OK result code {} with message: {}",
            result.getAck().getCode(),
            result.getAck().getMessage());
      }

      if (result.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = result
          .getResponses(0); // only head (end-result) is important

      List<QueryResultTupleMessage> results = response.getResultsList();
      _return.addAll(resultsToMap(results));
    }

    return _return;

  }

  @Override
  public <T extends DistanceElement> List<T> getNearestNeighboursGeneric(int k, float[] vector,
      String column, Class<T> distanceElementClass, ReadableQueryConfig config) {

    NearestNeighbourQueryMessage nnqMessage = mb.buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);
    QueryMessage sqMessage = this.mb.buildQueryMessage(config.getHints().isEmpty() ? ADAMproMessageBuilder.DEFAULT_HINT : config.getHints(), fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);

    ArrayList<QueryResultsMessage> resultList = this.adampro
        .streamingStandardQuery(sqMessage);

    if (resultList.isEmpty()){
      return Collections.emptyList();
    }

    List<T> _return = new ArrayList<>(k);

    for (QueryResultsMessage result : resultList){
      AckMessage ack = result.getAck();
      if (ack.getCode() != AckMessage.Code.OK) {
        LOGGER.error("error in getNearestNeighbours on entity {}, ({}) : {}", entityName, ack.getCode(), ack.getMessage());
        LOGGER.error("Query was {} ",sqMessage.toString());
        continue;
      }

      if (result.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important
      _return.addAll(handleNearestNeighbourResponse(response, k, distanceElementClass));
    }

    return _return;
  }

  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs) {

    if(vectors == null || vectors.isEmpty()){
      return Collections.emptyList();
    }

    List<QueryMessage> messages = new ArrayList<>(vectors.size());

    for(int i = 0; i < vectors.size(); ++i) {
      float[] vector = vectors.get(i);
      ReadableQueryConfig config = configs.get(i);

      NearestNeighbourQueryMessage nnqMessage = mb.buildNearestNeighbourQueryMessage(column,
          DataMessageConverter.convertVectorMessage(vector), k, config);
      QueryMessage sqMessage = this.mb
          .buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fromMessage, null,
              ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);

      messages.add(sqMessage);


    }
    ArrayList<QueryResultsMessage> resultList = this.adampro
        .streamingStandardQuery(messages);

    if (resultList.isEmpty()){
      return Collections.emptyList();
    }

    List<T> _return = new ArrayList<>(k);

    for (QueryResultsMessage result : resultList){
      AckMessage ack = result.getAck();
      if (ack.getCode() != AckMessage.Code.OK) {
        LOGGER.error("error in getBatchedNearestNeighbours on entity {}, ({}) : {}", entityName, ack.getCode(), ack.getMessage());
        return new ArrayList<>(0);
      }

      if (result.getResponsesCount() == 0) {
        return new ArrayList<>(0);
      }

      QueryResultInfoMessage response = result.getResponses(0); // only head (end-result) is important
      _return.addAll(handleNearestNeighbourResponse(response, k, distanceElementClass));
    }

    return _return;

  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) {
    /* Check if sizes of configs and vectors array correspond. */
    if (vectors.size() > configs.size()) {
      throw new IllegalArgumentException("You must provide a separate QueryConfig entry for each vector - even if it is the same instance of the QueryConfig.");
    }

    /* Prepare list of QueryMessages. */
    List<SubExpressionQueryMessage> queryMessages = new ArrayList<>(vectors.size());
    for (int i = 0; i < vectors.size(); i++) {
      float[] vector = vectors.get(i);
      ReadableQueryConfig config = configs.get(i);

      /* Extract hints from QueryConfig. If they're not set, then replace by DEFAULT_HINT. */
      Collection<Hints> hints;
      if (!config.getHints().isEmpty()) {
        hints = config.getHints();
      } else {
        hints = ADAMproMessageBuilder.DEFAULT_HINT;
      }
      NearestNeighbourQueryMessage nnqMessage = this.mb.buildNearestNeighbourQueryMessage(column, DataMessageConverter.convertVectorMessage(vector), k, config);
      QueryMessage qMessage = this.mb.buildQueryMessage(hints, this.fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);
      queryMessages.add(this.mb.buildSubExpressionQueryMessage(qMessage));
    }

    /* Constructs the correct SubExpressionQueryMessage bassed on the mergeOperation. */
    SubExpressionQueryMessage seqm;
    switch (merge) {
      case UNION:
        seqm = this.mb.mergeSubexpressions(queryMessages, AdamGrpc.ExpressionQueryMessage.Operation.FUZZYUNION, options);
        break;
      case INTERSECT:
        seqm = this.mb.mergeSubexpressions(queryMessages, AdamGrpc.ExpressionQueryMessage.Operation.FUZZYINTERSECT, options);
        break;
      case EXCEPT:
        seqm = this.mb.mergeSubexpressions(queryMessages, AdamGrpc.ExpressionQueryMessage.Operation.EXCEPT, options);
        break;
      default:
        seqm = this.mb.mergeSubexpressions(queryMessages, AdamGrpc.ExpressionQueryMessage.Operation.FUZZYUNION, options);
        break;
    }

    FromMessage fromMessage = this.mb.buildFromSubExpressionMessage(seqm);
    QueryMessage sqMessage = this.mb.buildQueryMessage(null, fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, null);

    List<QueryResultsMessage> resultList = this.adampro.streamingStandardQuery(sqMessage);

    List<T> _return = new ArrayList<>();

    for(QueryResultsMessage result : resultList) {

      AckMessage ack = result.getAck();
      if (ack.getCode() != AckMessage.Code.OK) {
        LOGGER.error("error in getNearestNeighbours on entity {}, (Code {}) : {}", entityName,
            ack.getCode(), ack.getMessage());
        LOGGER.error("Query was {} ", sqMessage.toString());
        continue;
      }

      if (result.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = result
          .getResponses(0); // only head (end-result) is important
      _return.addAll(handleNearestNeighbourResponse(response, k, distanceElementClass));
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {
    NearestNeighbourQueryMessage nnqMessage = this.mb.buildNearestNeighbourQueryMessage(column,
        DataMessageConverter.convertVectorMessage(vector), k, config);

    /* Extract hints from QueryConfig. If they're not set, then replace by DEFAULT_HINT. */
    Collection<ReadableQueryConfig.Hints> hints;
    if (!config.getHints().isEmpty()) {
      hints = config.getHints();
    } else {
      hints = ADAMproMessageBuilder.DEFAULT_HINT;
    }

    QueryMessage sqMessage = this.mb.buildQueryMessage(hints, this.fromMessage, null, null, nnqMessage);

    ArrayList<QueryResultsMessage> resultList = this.adampro.streamingStandardQuery(sqMessage);

    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(k);

    for(QueryResultsMessage result: resultList) {

      if (result.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = result
          .getResponses(0); // only head (end-result) is important

      AckMessage ack = response.getAck();
      if (ack.getCode() != Code.OK) {
        LOGGER.error("error in getNearestNeighbourRows, entity {} ({}) : {}", entityName,
            ack.getCode(), ack.getMessage());
       continue;
      }
      _return.addAll(resultsToMap(response.getResultsList()));
    }
    return _return;
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, this.mb.buildBooleanQueryMessage(this.mb.buildWhereMessage(fieldName, value)), null, null);

    ArrayList<QueryResultsMessage> resultList = this.adampro.streamingStandardQuery(qbqm);
    ArrayList<float[]> _return = new ArrayList<>();


    for(QueryResultsMessage r : resultList) {

      if (r.getResponsesCount() == 0) {
        continue;
      }

      QueryResultInfoMessage response = r.getResponses(0); // only head (end-result) is important

      AckMessage ack = response.getAck();
      if (ack.getCode() != Code.OK) {
        LOGGER
            .error("error in getFeatureVectors on entity {}, ({}) : {}", entityName, ack.getCode(),
                ack.getMessage());
        continue;
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
    }

    return _return;

  }


  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname,
      String... terms) {
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

    QueryMessage qm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fmBuilder, null, null, null);

    return executeQuery(qm);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<String> values) {
    if (values == null || Iterables.isEmpty(values)) {
      return new ArrayList<>(0);
    }

    /* TODO: Escape quotes. */
    final WhereMessage where = this.mb.buildWhereMessage(fieldName, values, operator);
    final BooleanQueryMessage bqMessage = this.mb.buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
  }

  private List<Map<String, PrimitiveTypeProvider>> executeBooleanQuery(BooleanQueryMessage bqm) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, bqm, null, null);
    return executeQuery(qbqm);
  }



}
