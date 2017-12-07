package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

/**
 * Helper class to disable database lookups.
 *
 * @author Luca Rossetto
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
  public <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector, String column,
      Class<T> distanceElementClass, ReadableQueryConfig config) {
    return new ArrayList<>(0);
  }

  /**
   * Performs a batched kNN-search with multiple vectors. That is, ADAM pro is tasked to perform the kNN search for each vector in the
   * provided list and return results of each query.
   *
   * @param k                    The number k vectors to return per query.
   * @param vectors              The list of vectors to use.
   * @param column               The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs              The query configuration, which may contain distance definitions or query-hints.
   * @return List of results.
   */
  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs) {
    return new ArrayList<>(0);
  }

  /**
   * Performs a combined kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector and then
   * merge the partial result sets pairwise using the desired MergeOperation.
   *
   * @param k                    The number k vectors to return per query.
   * @param vectors              The list of vectors to use.
   * @param column               The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs              The query configuration, which may contain distance definitions or query-hints.
   * @param merge
   * @param options
   * @return List of results.
   */
  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) {
    return new ArrayList<>(0);
  }


  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {
    return new ArrayList<>(0);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      Iterable<String> values) {
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

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean existsEntity(String name) {
    return false;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, String value) {
    return new ArrayList<>(0);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<String> values) {
    return new ArrayList<>(0);
  }
}
