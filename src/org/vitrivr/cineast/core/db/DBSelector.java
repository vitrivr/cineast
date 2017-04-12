package org.vitrivr.cineast.core.db;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.DistanceElement;

public interface DBSelector {

  boolean open(String name);

  boolean close();

  /**
   * Finds the {@code k}-nearest neighbours of the given {@code vector} in {@code column} using the
   * provided distance function in {@code config}. {@code scoreElementClass} defines the specific
   * type of {@link DistanceElement} to be returned by this method.
   *
   * @param k maximum number of results
   * @param vector query vector
   * @param column feature column to do the search
   * @param scoreElementClass class of the {@link DistanceElement} type
   * @param config query config
   * @param <T> type of the {@link DistanceElement}
   * @return a list of elements with their distance
   */
  <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector, String column,
      Class<T> scoreElementClass, ReadableQueryConfig config);

  List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config);

  List<float[]> getFeatureVectors(String fieldName, String value, String vectorName);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<String> values);

  /**
   * SELECT column from the table. Be careful with large entities
   */
  List<PrimitiveTypeProvider> getAll(String column);

  /**
   * SELECT * from
   */
  List<Map<String, PrimitiveTypeProvider>> getAll();

  boolean existsEntity(String name);

  /**
   * Get first k rows
   */
  List<Map<String, PrimitiveTypeProvider>> preview(int k);
}
