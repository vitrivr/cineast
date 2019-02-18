package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.AtomicLiteralBooleanPredicate.Operator;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
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

    Projection projection = null; //TODO

    List<QueryResponseMessage> results = this.cottontail
        .query(
            CottontailMessageBuilder.queryMessage(
                CottontailMessageBuilder.query(entity, projection, CottontailMessageBuilder
                    .atomicWhere(fieldName, Operator.IN, false,
                        CottontailMessageBuilder.toData(values)), null)
                , ""));

    return null; //TODO
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
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return null;
  }

  @Override
  public boolean existsEntity(String name) {
    return false;
  }

  @Override
  public boolean ping() {
    return false;
  }
}
