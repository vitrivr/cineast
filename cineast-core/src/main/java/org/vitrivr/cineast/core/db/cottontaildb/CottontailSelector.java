package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.query;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.queryMessage;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.toData;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.toDatas;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.whereInList;
import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Help.Column;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ConnectionOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Knn;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionOperation;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage.Tuple;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Where;

public class CottontailSelector implements DBSelector {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Projection SELECT_ALL_PROJECTION = CottontailMessageBuilder.projection(ProjectionOperation.SELECT, "*");

  private final CottontailWrapper cottontail;
  private EntityName entity;

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
          CottontailMessageBuilder.projection(ProjectionOperation.SELECT, GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER),
          whereInList(GENERIC_ID_COLUMN_QUALIFIER, config.getRelevantSegmentIds()),
          knn, k));
    return toDistanceElement(this.cottontail.query(queryMessage), distanceElementClass);
  }

  @Override
  public <E extends DistanceElement> List<E> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<E> distanceElementClass, List<ReadableQueryConfig> configs) {
    Query query = query(
        entity,
        CottontailMessageBuilder.projection(ProjectionOperation.SELECT, GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER),
        whereInList(GENERIC_ID_COLUMN_QUALIFIER, configs.get(0).getRelevantSegmentIds()),
        CottontailMessageBuilder.batchedKnn(column, vectors, null, k, configs.get(0).getDistance().orElse(Distance.manhattan)), null);

    return toDistanceElement(this.cottontail.query(queryMessage(query)), distanceElementClass);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {

    Knn knn = CottontailMessageBuilder.knn(column, vector, config.getDistanceWeights().orElse(null), k, config.getDistance().orElse(Distance.manhattan));
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, whereInList(GENERIC_ID_COLUMN_QUALIFIER, config.getRelevantSegmentIds()), knn, k));
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {

    Projection projection = CottontailMessageBuilder.projection(ProjectionOperation.SELECT, vectorName);
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

    final List<QueryResponseMessage> results = this.cottontail.query(queryMessage(query(entity, projection, where, null, null)));
    final List<float[]> _return = new ArrayList<>();
    final ColumnName column = CottontailMessageBuilder.column(vectorName);
    for (QueryResponseMessage response : results) {
      final int index = response.getColumnsList().indexOf(column);
      for (Tuple t : response.getTuplesList()) {
        _return.add(CottontailMessageBuilder.fromData(t.getData(index)).getFloatArray());
      }
    }
    return _return;
  }

  @Override
  public List<PrimitiveTypeProvider> getFeatureVectorsGeneric(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    final Projection projection = CottontailMessageBuilder.projection(ProjectionOperation.SELECT, vectorName);
    final Where where = CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));
    final List<QueryResponseMessage> results = this.cottontail.query(queryMessage(query(entity, projection, where, null, null)));
    return toSingleCol(results, vectorName);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    return getRows(fieldName, toDatas(values).toArray(new Literal[0]));
  }

  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Literal... values) {
    final QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.IN, values), null, null));
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values) {
    return getRows(fieldName, toDatas(values).toArray(new Literal[0]));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(
      int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {

    /* This includes the filter for segmentids in the where-statement */
    Where where = CottontailMessageBuilder.compoundWhere(queryConfig, fieldname, RelationalOperator.LIKE, ConnectionOperator.OR, toData(Arrays.asList(terms)));

    final Projection projection = CottontailMessageBuilder.projection(ProjectionOperation.SELECT, GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER);
    final QueryMessage queryMessage = queryMessage(query(entity, projection, where, null, rows));
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, operator, toDatas(values).toArray(new Literal[0]));
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, where, null, null));
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig qc) {

    Projection proj = projection.isEmpty() ? SELECT_ALL_PROJECTION : CottontailMessageBuilder.projection(ProjectionOperation.SELECT, projection.toArray(new String[]{}));
    Where where = CottontailMessageBuilder.compoundWhere(qc, conditions);
    QueryMessage queryMessage = queryMessage(query(entity, proj, where, null, null));
    return processResults(this.cottontail.query(queryMessage));
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    Projection projection = CottontailMessageBuilder.projection(ProjectionOperation.SELECT, column);
    QueryMessage queryMessage = queryMessage(query(entity, projection, null, null, null));
    List<QueryResponseMessage> results = this.cottontail.query(queryMessage);

    return toSingleCol(results, column);
  }

  @Override
  public List<PrimitiveTypeProvider> getUniqueValues(String column) {
    QueryMessage queryMessage = queryMessage(
        query(entity, CottontailMessageBuilder.projection(ProjectionOperation.SELECT_DISTINCT, column), null, null, null));
    List<QueryResponseMessage> results = this.cottontail.query(queryMessage);
    return toSingleCol(results, column);
  }

  public Map<String, Integer> countDistinctValues(String column) {
    final QueryMessage queryMessage = queryMessage(
        query(entity, CottontailMessageBuilder.projection(ProjectionOperation.SELECT, column), null, null, null));
    final Map<String, Integer> count = new HashMap<>();
    final List<QueryResponseMessage> list = this.cottontail.query(queryMessage);
    for (QueryResponseMessage m : list) {
      int index = m.getColumnsList().indexOf(CottontailMessageBuilder.column(column));
      for (Tuple t : m.getTuplesList()) {
        count.merge(t.getData(index).getStringData(), 1, (old, one) -> old + 1);
      }
    }
    return count;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    QueryMessage queryMessage = queryMessage(query(entity, SELECT_ALL_PROJECTION, null, null, null));
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
    final List<Map<String, PrimitiveTypeProvider>> _return = new LinkedList<>();
    final StopWatch watch = StopWatch.createStarted();

    for (QueryResponseMessage response : queryResponses) {
      for (Tuple t : response.getTuplesList()) {
          final Map<String,PrimitiveTypeProvider> map = new HashMap<>(response.getColumnsCount());
          int i = 0;
          for (ColumnName c : response.getColumnsList()) {
            map.put(c.getName(), CottontailMessageBuilder.fromData(t.getData(i++)));
          }
          _return.add(map);
      }
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
    final List<PrimitiveTypeProvider> _return = new LinkedList<>();
    for (QueryResponseMessage response : results) {
      int index = response.getColumnsList().indexOf(CottontailMessageBuilder.column(colName));
      for (Tuple t : response.getTuplesList()) {
        _return.add(CottontailMessageBuilder.fromData(t.getData(index)));
      }
    }
    return _return;
  }

  private static <T extends DistanceElement> List<T> handleNearestNeighbourResponse(QueryResponseMessage response, Class<? extends T> distanceElementClass) {
    List<T> result = new ArrayList<>();
    int idIndex = response.getColumnsList().indexOf(CottontailMessageBuilder.column(GENERIC_ID_COLUMN_QUALIFIER));
    int distanceIndex = response.getColumnsList().indexOf(CottontailMessageBuilder.column("distance"));
    for (Tuple t : response.getTuplesList()) {
      String id = null;
      final Literal data = t.getData(idIndex);
      switch (data.getDataCase()) {
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
      double distance = t.getData(distanceIndex).getDoubleData();
      T e = DistanceElement.create(distanceElementClass, id, distance);
      result.add(e);
    }

    return result;
  }


}
