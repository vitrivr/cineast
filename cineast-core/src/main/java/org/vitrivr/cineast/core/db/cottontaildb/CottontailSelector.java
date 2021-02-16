package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.query;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.queryMessage;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.toDatas;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.whereInList;
import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.CompoundBooleanPredicate.Operator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Data;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Entity;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Knn;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.Operation;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Tuple;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Where;

public class CottontailSelector implements DBSelector {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Projection SELECT_ALL_PROJECTION = CottontailMessageBuilder.projection(Operation.SELECT, "*");

  private final CottontailWrapper cottontail;
  private Entity entity;

  public CottontailSelector(CottontailWrapper wrapper) {
    this.cottontail = wrapper;
  }

  @Override
  public boolean open(String name) {
    this.entity = CottontailMessageBuilder.entity(name);
    return true;
  }

  @Override
  public boolean close() {
    this.cottontail.close();
    return true;
  }

  /**
   * if {@link ReadableQueryConfig#getRelevantSegmentIds()} is null, the where-clause will be left empty
   */
  @Override
  public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] vector, String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
    Knn knn = CottontailMessageBuilder.knn(column, vector, config.getDistanceWeights().orElse(null), k, config.getDistance().orElse(Distance.manhattan));
    QueryMessage queryMessage = queryMessage(
        query(entity,
            CottontailMessageBuilder.projection(Operation.SELECT, GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER),
            whereInList(GENERIC_ID_COLUMN_QUALIFIER, config.getRelevantSegmentIds()),
            knn,
            k),
        config.getQueryId().toString());
    return toDistanceElement(this.cottontail.query(queryMessage), distanceElementClass);
  }

  @Override
  public <E extends DistanceElement> List<E> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<E> distanceElementClass, List<ReadableQueryConfig> configs) {
    Query query = query(
        entity,
        CottontailMessageBuilder.projection(Operation.SELECT, GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER),
        whereInList(GENERIC_ID_COLUMN_QUALIFIER, configs.get(0).getRelevantSegmentIds()),
        CottontailMessageBuilder.batchedKnn(column, vectors, null, k, configs.get(0).getDistance().orElse(Distance.manhattan)), null);

    return toDistanceElement(this.cottontail.query(queryMessage(query, configs.get(0).getQueryId().toString())), distanceElementClass);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {

    Knn knn = CottontailMessageBuilder.knn(column, vector, config.getDistanceWeights().orElse(null), k, config.getDistance().orElse(Distance.manhattan));
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, whereInList(GENERIC_ID_COLUMN_QUALIFIER, config.getRelevantSegmentIds()), knn, k), config.getQueryId().toString());
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, vectorName);
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

    List<QueryResponseMessage> results = this.cottontail.query(queryMessage(query(entity, projection, where, null, null), null));

    List<float[]> _return = new ArrayList<>();

    for (QueryResponseMessage response : results) {
      for (Tuple t : response.getResultsList()) {
        _return.add(CottontailMessageBuilder.fromData(t.getDataMap().get(vectorName)).getFloatArray());
      }
    }

    return _return;
  }

  @Override
  public List<PrimitiveTypeProvider> getFeatureVectorsGeneric(String fieldName, PrimitiveTypeProvider value, String vectorName) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, vectorName);
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

    List<QueryResponseMessage> results = this.cottontail.query(queryMessage(query(entity, projection, where, null, null), null));

    return toSingleCol(results, vectorName);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    return getRows(fieldName, toDatas(values));
  }

  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Data... values) {
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.IN, values), null, null), null);
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values) {
    return getRows(fieldName, toDatas(values));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(
      int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {

    /* This includes the filter for segmentids in the where-statement */
    Where where = CottontailMessageBuilder.compoundWhere(queryConfig, fieldname, RelationalOperator.LIKE, Operator.OR, toDatas(Arrays.asList(terms)));

    final Projection projection = Projection.newBuilder().setOp(Operation.SELECT).putAttributes(GENERIC_ID_COLUMN_QUALIFIER, "").putAttributes("score", DB_DISTANCE_VALUE_QUALIFIER).build();
    QueryMessage queryMessage = queryMessage(query(entity, projection, where, null, rows), null);
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, operator, toDatas(values));
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, where, null, null), null);
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig qc) {

    Projection proj = projection.isEmpty() ? SELECT_ALL_PROJECTION : CottontailMessageBuilder.projection(Operation.SELECT, projection.toArray(new String[]{}));
    Where where = CottontailMessageBuilder.compoundWhere(qc, conditions);
    QueryMessage queryMessage = queryMessage(query(entity, proj, where, null, null), null);
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, column);
    QueryMessage queryMessage = queryMessage(query(entity, projection, null, null, null), null);
    List<QueryResponseMessage> results = this.cottontail.query(queryMessage);

    return toSingleCol(results, column);
  }

  @Override
  public List<PrimitiveTypeProvider> getUniqueValues(String column) {
    QueryMessage queryMessage = queryMessage(
        query(entity, CottontailMessageBuilder.projection(Operation.SELECT_DISTINCT, column), null, null, null), null);
    List<QueryResponseMessage> results = this.cottontail.query(queryMessage);
    return toSingleCol(results, column);
  }

  public Map<String, Integer> countDistinctValues(String column) {
    QueryMessage queryMessage = queryMessage(
        query(entity, CottontailMessageBuilder.projection(Operation.SELECT, column), null, null,
            null), null);
    Map<String, Integer> count = new HashMap<>();
    List<QueryResponseMessage> list = this.cottontail.query(queryMessage);
    list.forEach(row -> row.getResultsList().forEach(tuple -> {
      count.merge(tuple.getDataMap().get(column).getStringData(), 1, (old, one) -> old + 1);
    }));
    return count;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, null, null, null), null);
    return processResults(this.cottontail.query(queryMessage));

  }

  @Override
  public boolean existsEntity(String name) {
    return this.cottontail.existsEntity(name);
  }

  @Override
  public boolean ping() { // currently not supported
    return this.cottontail.ping();
  }

  public static List<Map<String, PrimitiveTypeProvider>> processResults(List<QueryResponseMessage> queryResponses) {
    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();
    StopWatch watch = StopWatch.createStarted();

    for (QueryResponseMessage response : queryResponses) {
      response.getResultsList().forEach(tuple -> _return.add(CottontailMessageBuilder.tupleToMap(tuple)));
    }
    LOGGER.trace("Processed {} results in {} ms", _return.size(), watch.getTime(TimeUnit.MILLISECONDS));

    return _return;
  }

  private <E extends DistanceElement> List<E> toDistanceElement(List<QueryResponseMessage> results, Class<E> distanceElementClass) {
    List<E> _return = new ArrayList<>();

    for (QueryResponseMessage r : results) {
      _return.addAll(handleNearestNeighbourResponse(r, distanceElementClass));
    }

    return _return;
  }

  private List<PrimitiveTypeProvider> toSingleCol(List<QueryResponseMessage> results, String colName) {
    List<PrimitiveTypeProvider> _return = new ArrayList<>();

    for (QueryResponseMessage response : results) {
      for (Tuple t : response.getResultsList()) {
        _return.add(CottontailMessageBuilder.fromData(t.getDataMap().get(colName)));
      }
    }

    return _return;
  }

  private static <T extends DistanceElement> List<T> handleNearestNeighbourResponse(QueryResponseMessage response, Class<? extends T> distanceElementClass) {
    List<T> result = new ArrayList<>();
    for (Tuple t : response.getResultsList()) {
      String id = null;
      Data data = t.getDataMap().get(GENERIC_ID_COLUMN_QUALIFIER);
      switch (t.getDataMap().get(GENERIC_ID_COLUMN_QUALIFIER).getDataCase()) {
        case BOOLEANDATA:
        case VECTORDATA:
        case DATA_NOT_SET:
        case NULLDATA:
          continue;
        case INTDATA:
          id = String.valueOf(data.getIntData());
          break;
        case LONGDATA:
          id = String.valueOf(data.getLongData());
          break;
        case FLOATDATA:
          id = String.valueOf(data.getFloatData());
          break;
        case DOUBLEDATA:
          id = String.valueOf(data.getDoubleData());
          break;
        case STRINGDATA:
          id = data.getStringData();
          break;
      }
      if (id == null) {
        continue;
      }
      double distance = t.getDataMap().get("distance").getDoubleData(); // TODO what key is used for the distance?
      T e = DistanceElement.create(distanceElementClass, id, distance);
      result.add(e);
    }

    return result;
  }


}
