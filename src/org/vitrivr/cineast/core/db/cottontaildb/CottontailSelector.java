package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.AtomicLiteralBooleanPredicate.Operator;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection.Operation;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
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
      List<ReadableQueryConfig> configs) {
    return null;
  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) {
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {
    return null;
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    return null;
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
      String... terms) {
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<String> values) {
    return null;
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
    return false;
  }

  @Override
  public boolean ping() {
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
