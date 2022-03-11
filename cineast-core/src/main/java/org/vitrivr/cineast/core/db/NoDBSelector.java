package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

/**
 * Helper class to disable database lookups.
 */
public class NoDBSelector implements DBSelector {

  @Override
  public boolean open(String name) {
    return true;
  }

  @Override
  public void close() {
  }

  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
    return new ArrayList<>(0);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName, ReadableQueryConfig queryConfig) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    return new ArrayList<>(0);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean existsEntity(String name) {
    return false;
  }

  @Override
  public boolean ping() {
    return true;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    return new ArrayList<>(0);
  }
}
