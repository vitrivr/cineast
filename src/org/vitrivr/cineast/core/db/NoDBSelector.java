package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

/**
 * 
 * @author Luca Rossetto
 * 
 * Helper class to disable database lookups
 *
 */
public class NoDBSelector implements DBSelector {

  @Override
  public boolean open(String name) {
    return true;
  }

  @Override
  public boolean close() {
    return true;
  }

  @Override
  public List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column,
      QueryConfig config) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, QueryConfig config) {
    return new ArrayList<>(0);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
    return new ArrayList<>(0);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    return new ArrayList<>(0);
  }

}
