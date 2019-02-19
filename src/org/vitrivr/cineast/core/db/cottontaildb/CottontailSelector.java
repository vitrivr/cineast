package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.CINEAST_SCHEMA;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection.Operation;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
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

  private Entity entity;

  private static final Projection SELECT_ALL_PROJECTION = CottontailMessageBuilder.projection(Operation.SELECT, "*");

  @Override
  public boolean open(String name) {
    this.entity = CottontailMessageBuilder.entity(name);
    return true;
  }

  @Override
  public boolean close() {
    if (useGlobalWrapper) {
      return false;
    }
    this.cottontail.close();
    return true;
  }

  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs) { //TODO
    return null;
  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) { //TODO
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) { //TODO
    return null;
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, vectorName);
    Where where = CottontailMessageBuilder.atomicWhere(fieldName, RelationalOperator.EQ, CottontailMessageBuilder.toData(value));

    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, where, null),
             ""));

    List<float[]> _return = new ArrayList<>();

    for(QueryResponseMessage response : results){
      for(Tuple t : response.getResultsList()){
        _return.add(CottontailMessageBuilder.fromData(t.getDataMap().get(vectorName)).getFloatArray());
      }
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      Iterable<String> values) {

    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, CottontailMessageBuilder
                    .atomicWhere(fieldName, RelationalOperator.IN,
                        CottontailMessageBuilder.toData(values)), null)
                , ""));

    return processResults(results);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname,
      String... terms) { //TODO
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<String> values) {

    Where where = CottontailMessageBuilder.atomicWhere(fieldName, operator, CottontailMessageBuilder.toDatas(values));

    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, where, null), "")
        );


    return processResults(results);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {

    Projection projection = CottontailMessageBuilder.projection(Operation.SELECT, column);

    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, null, null)
                , ""));

    List<PrimitiveTypeProvider> _return = new ArrayList<>();

    for(QueryResponseMessage response : results){
      for(Tuple t : response.getResultsList()){
        _return.add(CottontailMessageBuilder.fromData(t.getDataMap().get(column)));
      }
    }

    return _return;

  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, SELECT_ALL_PROJECTION, null, null)
                , ""));

    return processResults(results);
  }

  @Override
  public boolean existsEntity(String name) {

    List<Entity> entities = this.cottontail.listEntities(CINEAST_SCHEMA);

    for(Entity entity: entities){
      if(entity.getName().equals(name)){
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean ping() { //currently not supported
    return false;
  }

  private static List<Map<String, PrimitiveTypeProvider>> processResults(Iterable<QueryResponseMessage> qureyresponses){
    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();

    for(QueryResponseMessage response : qureyresponses){
      for(Tuple t : response.getResultsList()){
        _return.add(CottontailMessageBuilder.tupleToMap(t));
      }
    }

    return _return;

  }


}
