package org.vitrivr.cineast.core.db.adampro;

import com.google.common.collect.Iterables;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc.*;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.RelationalOperator;

import java.util.*;

@Deprecated
public class ADAMproStreamingSelector extends AbstractADAMproSelector {

  private static final Logger LOGGER = LogManager.getLogger();

  public ADAMproStreamingSelector(ADAMproWrapper wrapper) {
    super(wrapper);
  }

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
    QueryMessage sqMessage = this.mb.buildQueryMessage(config.getHints().isEmpty() ? ADAMproMessageBuilder.DEFAULT_HINT : config.getHints(), fromMessage, this.mb.inList("id", config.getRelevantSegmentIds()), ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);

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
              .buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fromMessage,
                      this.mb.inList("id", config.getRelevantSegmentIds()),
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

    QueryMessage sqMessage = this.mb.buildQueryMessage(hints, this.fromMessage, this.mb.inList("id", config.getRelevantSegmentIds()), null, nnqMessage);

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
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, this.mb.buildBooleanQueryMessage(this.mb.buildWhereMessage(fieldName, value.getString())), null, null);

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
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig,
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

    QueryMessage qm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fmBuilder, this.mb.inList("id", queryConfig.getRelevantSegmentIds()), null, null);

    return executeQuery(qm);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    if (values == null || Iterables.isEmpty(values)) {
      return new ArrayList<>(0);
    }

    /* TODO: Escape quotes. */
    final WhereMessage where = this.mb.buildWhereMessage(fieldName, StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::getString).collect(Collectors.toList()), operator);
    final BooleanQueryMessage bqMessage = this.mb.buildBooleanQueryMessage(where);
    return executeBooleanQuery(bqMessage);
  }

  private List<Map<String, PrimitiveTypeProvider>> executeBooleanQuery(BooleanQueryMessage bqm) {
    QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, bqm, null, null);
    return executeQuery(qbqm);
  }



}
