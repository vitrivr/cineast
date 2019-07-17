package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.CINEAST_SCHEMA;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Knn;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection.Operation;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Query;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.MergeOperation;
import org.vitrivr.cineast.core.db.RelationalOperator;

public class CottontailSelector implements DBSelector {

  private static boolean useGlobalWrapper = true;
  private static final CottontailWrapper GLOBAL_COTTONTAIL_WRAPPER =
      useGlobalWrapper ? new CottontailWrapper() : null;
  private CottontailWrapper cottontail =
      useGlobalWrapper ? GLOBAL_COTTONTAIL_WRAPPER : new CottontailWrapper();

  private static final Logger LOGGER = LogManager.getLogger();

  private Entity entity;

  private static final Projection SELECT_ALL_PROJECTION =
      CottontailMessageBuilder.projection(Operation.SELECT, "*");

  @Override
  public boolean open(String name) {
    this.entity = CottontailMessageBuilder.entity(name);
    return true;
  }

  @Override
  public boolean close() {
    if (useGlobalWrapper) {
      LOGGER.info("Not closing selector because the global wrapper flag is set");
      return false;
    }
    this.cottontail.close();
    return true;
  }

  @Override
  public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(
      int k,
      float[] vector,
      String column,
      Class<E> distanceElementClass,
      ReadableQueryConfig config) {

    Knn knn =
        CottontailMessageBuilder.knn(
            column,
            vector,
            config.getDistanceWeights().orElse(null),
            k,
            config.getDistance().orElse(Distance.manhattan));

    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, CottontailMessageBuilder.projection(Operation.SELECT, "id", "distance"), null, knn),
                config.getQueryId().toString()));

    List<E> _return = new ArrayList<>();

    for (QueryResponseMessage r : results) {
      _return.addAll(handleNearestNeighbourResponse(r, distanceElementClass));
    }

    return _return;
  }

  @Override
  public <E extends DistanceElement> List<E> getBatchedNearestNeighbours(
      int k,
      List<float[]> vectors,
      String column,
      Class<E> distanceElementClass,
      List<ReadableQueryConfig> configs) {

    Query query = CottontailMessageBuilder.query(
        entity,
        CottontailMessageBuilder.projection(Operation.SELECT, "id", "distance"),
        null,
        CottontailMessageBuilder.batchedKnn(
            column,
            vectors,
            null,
            k,
            configs.get(0).getDistance().orElse(Distance.manhattan)));

    List<QueryResponseMessage> results = this.cottontail.query(CottontailMessageBuilder.queryMessage(query, configs.get(0).getQueryId().toString()));

    List<E> _return = new ArrayList<>();

    for (QueryResponseMessage r : results) {
      _return.addAll(handleNearestNeighbourResponse(r, distanceElementClass));
    }

    return _return;
  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(
      int k,
      List<float[]> vectors,
      String column,
      Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs,
      MergeOperation merge,
      Map<String, String> options) { // TODO
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(
      int k, float[] vector, String column, ReadableQueryConfig config) {

    Knn knn =
        CottontailMessageBuilder.knn(
            column,
            vector,
            config.getDistanceWeights().orElse(null),
            k,
            config.getDistance().orElse(Distance.manhattan));

    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, null, knn),
                config.getQueryId().toString()));

    return processResults(results);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, vectorName);
    Where where =
        CottontailMessageBuilder.atomicWhere(
            fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, where, null), ""));

    List<float[]> _return = new ArrayList<>();

    for (QueryResponseMessage response : results) {
      for (Tuple t : response.getResultsList()) {
        _return.add(
            CottontailMessageBuilder.fromData(t.getDataMap().get(vectorName)).getFloatArray());
      }
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(
      String fieldName, Iterable<String> values) {

    CottontailGrpc.Data[] array = new CottontailGrpc.Data[Iterables.size(values)];
    int i = 0;
    for (String s : values) {
      array[i] = CottontailMessageBuilder.toData(s);
      i++;
    }
    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.IN, array),
                    null),
                ""));

    return processResults(results);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(
      int rows, String fieldname, String... terms) {

    final String searchString = Joiner.on(' ').join(terms); //TODO there might be a better way to do this?

    final Where where = CottontailMessageBuilder.atomicWhere(fieldname, RelationalOperator.LIKE, CottontailMessageBuilder.toData(searchString));
    final Projection projection = Projection.newBuilder().setOp(Operation.SELECT).putAttributes("id", "").putAttributes("score", "ap_score").build();

    final List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, where, null), ""));

    return processResults(results);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(
      String fieldName, RelationalOperator operator, Iterable<String> values) {

    Where where =
        CottontailMessageBuilder.atomicWhere(
            fieldName, operator, CottontailMessageBuilder.toDatas(values));

    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, where, null), ""));

    return processResults(results);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, column);

    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, null, null), ""));

    List<PrimitiveTypeProvider> _return = new ArrayList<>();

    for (QueryResponseMessage response : results) {
      for (Tuple t : response.getResultsList()) {
        _return.add(CottontailMessageBuilder.fromData(t.getDataMap().get(column)));
      }
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    List<QueryResponseMessage> results =
        this.cottontail.query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, null, null), ""));

    return processResults(results);
  }

  @Override
  public boolean existsEntity(String name) {

    List<Entity> entities = this.cottontail.listEntities(CINEAST_SCHEMA);

    for (Entity entity : entities) {
      if (entity.getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean ping() { // currently not supported
    return this.cottontail.ping();
  }

  private static List<Map<String, PrimitiveTypeProvider>> processResults(
      Iterable<QueryResponseMessage> qureyresponses) {
    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();

    for (QueryResponseMessage response : qureyresponses) {
      for (Tuple t : response.getResultsList()) {
        _return.add(CottontailMessageBuilder.tupleToMap(t));
      }
    }

    return _return;
  }

  private static <T extends DistanceElement> List<T> handleNearestNeighbourResponse(
      QueryResponseMessage response, Class<? extends T> distanceElementClass) {
    List<T> result = new ArrayList<>();
    for (Tuple t : response.getResultsList()) {
      String id = t.getDataMap().get("id").getStringData();
      if (id == null) {
        continue;
      }
      double distance =
          t.getDataMap().get("distance").getDoubleData(); // TODO what key is used for the distance?
      T e = DistanceElement.create(distanceElementClass, id, distance);
      result.add(e);
    }

    return result;
  }
}
