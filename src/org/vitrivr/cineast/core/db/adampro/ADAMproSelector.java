package org.vitrivr.cineast.core.db.adampro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.Code;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BooleanQueryMessage.WhereMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DataMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.DenseVectorMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExternalHandlerQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.FromMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.NearestNeighbourQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultInfoMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultTupleMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.SubExpressionQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.VectorMessage;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.DefaultValueHashMap;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DataMessageConverter;
import org.vitrivr.cineast.core.db.MergeOperation;
import org.vitrivr.cineast.core.util.LogHelper;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;

public class ADAMproSelector implements DBSelector {

    /**
     * flag to choose if every selector should have its own connection to ADAMpro or if they should
     * share one.
     */
    private static boolean useGlobalWrapper = true;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ADAMproWrapper GLOBAL_ADAMPRO_WRAPPER = useGlobalWrapper ? new ADAMproWrapper() : null;

    private ADAMproWrapper adampro = useGlobalWrapper ? GLOBAL_ADAMPRO_WRAPPER : new ADAMproWrapper();

    /** MessageBuilder instance used to create the query messages. */
    private final ADAMproMessageBuilder mb = new ADAMproMessageBuilder();

    /** Name of the entity the current instance of ADAMproSelector uses. */
    private String entityName;

    /** FromMessaged used by the instance of ADAMproSelector. */
    private FromMessage fromMessage;

    @Override
    public boolean open(String name) {
        this.entityName = name;
        this.fromMessage = this.mb.buildFromMessage(name);
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

    @Override
    public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
        QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage, this.mb.buildBooleanQueryMessage(this.mb.buildWhereMessage(fieldName, value)), null, null);

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
     * Performs a batched kNN-search with multiple vectors. That is, ADAM pro is tasked to perform the kNN search for each vector in the
     * provided list and return results of each query.
     *
     * @param k The number k vectors to return per query.
     * @param vectors The list of vectors to use.
     * @param column The column to perform the kNN search on.
     * @param distanceElementClass The class to use to create the resulting DistanceElements
     * @param configs The query configurations, which may contain distance definitions or query-hints. Every feature should have its own QueryConfig object.
     * @param <T> The type T of the resulting DistanceElements.
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
        for (int i = 0; i<vectors.size(); i++) {
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
            queryMessages.add(this.mb.buildQueryMessage(hints, this.fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage));
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
        for (int i = 0; i<result.getResultsCount(); i++) {
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

    /**
     * Performs a combined kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector and then
     * merge the partial result sets pairwise using the desired MergeOperation.
     *
     * @param k The number k vectors to return per query.
     * @param vectors The list of vectors to use.
     * @param column The column to perform the kNN search on.
     * @param distanceElementClass class of the {@link DistanceElement} type
     * @param configs The query configuration, which may contain distance definitions or query-hints.
     * @param <T>
     * @return
     */
    @Override
    public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs, MergeOperation mergeOperation, Map<String,String> options) {
        /* Check if sizes of configs and vectors array correspond. */
        if (vectors.size() > configs.size()) {
          throw new IllegalArgumentException("You must provide a separate QueryConfig entry for each vector - even if it is the same instance of the QueryConfig.");
        }

        /* Prepare list of QueryMessages. */
        List<SubExpressionQueryMessage> queryMessages = new ArrayList<>(vectors.size());
        for (int i = 0; i<vectors.size(); i++) {
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
            QueryMessage qMessage = this.mb.buildQueryMessage(hints, this.fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);
            queryMessages.add(this.mb.buildSubExpressionQueryMessage(qMessage));
        }

        /* Constructs the correct SubExpressionQueryMessage bassed on the mergeOperation. */
        SubExpressionQueryMessage seqm;
        switch (mergeOperation) {
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
        return handleNearestNeighbourResponse(response, k, distanceElementClass);
    }

    @Override
    public <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector, String column,
        Class<T> distanceElementClass, ReadableQueryConfig config) {
      NearestNeighbourQueryMessage nnqMessage = mb.buildNearestNeighbourQueryMessage(column,
          DataMessageConverter.convertVectorMessage(vector), k, config);
      QueryMessage sqMessage = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fromMessage, null, ADAMproMessageBuilder.DEFAULT_PROJECTION_MESSAGE, nnqMessage);
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
      return handleNearestNeighbourResponse(response, k, distanceElementClass);
  }

    
    private <T extends DistanceElement> List<T> handleNearestNeighbourResponse(QueryResultInfoMessage response, int k, Class<? extends T> distanceElementClass) {
      List<T> result = new ArrayList<>(k);
      for (QueryResultTupleMessage msg : response.getResultsList()) {
        String id = msg.getDataMap().get("id").getStringData();
        if (id == null) {
          continue;
        }
        double distance = msg.getDataMap().get("ap_distance").getDoubleData();
        T e = DistanceElement.create(distanceElementClass, id, distance);
        result.add(e);
      }

      return result;
  }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
        return getRows(fieldName, Collections.singleton(value));
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
        return getRows(fieldName, Arrays.asList(values));
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
                                                            Iterable<String> values) {
        if (values == null || Iterables.isEmpty(values)) {
            return new ArrayList<>(0);
        }

        WhereMessage where = this.mb.buildWhereMessage(fieldName, values);
        BooleanQueryMessage bqMessage = this.mb.buildBooleanQueryMessage(where);
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


    @Override
    public List<Map<String, PrimitiveTypeProvider>> getAll() {
        return preview(Integer.MAX_VALUE);
    }

    @Override
    public boolean existsEntity(String eName) {
        ListenableFuture<ExistsMessage> future = this.adampro.existsEntity(eName);
        try {
            return future.get().getExists();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("error in existsEntity, entitiy {}: {}", this.entityName, LogHelper.getStackTrace(e));
            return false;
        }
    }

    @Override
    public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
        PreviewMessage msg = PreviewMessage.newBuilder().setEntity(this.entityName).setN(k)
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
        QueryMessage qbqm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, this.fromMessage,  bqm, null, null);
        return executeQuery(qbqm);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
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

        QueryMessage sqMessage = this.mb.buildQueryMessage(hints, this.fromMessage, null, null, nnqMessage);

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



    public List<Map<String, PrimitiveTypeProvider>> getFromExternal(String externalHandlerName, Map<String, String> parameters) {

        ExternalHandlerQueryMessage.Builder ehqmBuilder = ExternalHandlerQueryMessage.newBuilder();
        ehqmBuilder.setEntity(this.entityName);
        ehqmBuilder.setHandler(externalHandlerName);

        ehqmBuilder.putAllParams(parameters);

        SubExpressionQueryMessage.Builder seqmBuilder = SubExpressionQueryMessage.newBuilder();
        seqmBuilder.setEhqm(ehqmBuilder);

        FromMessage.Builder fmBuilder = FromMessage.newBuilder();
        fmBuilder.setExpression(seqmBuilder);

        QueryMessage qm = this.mb.buildQueryMessage(ADAMproMessageBuilder.DEFAULT_HINT, fmBuilder, null, null, null);

        return executeQuery(qm);
    }

}
