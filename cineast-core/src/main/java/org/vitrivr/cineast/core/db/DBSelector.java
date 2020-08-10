package org.vitrivr.cineast.core.db;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;

public interface DBSelector {

  boolean open(String name);

  boolean close();

  /**
   * Convenience-wrapper to query with float-arrays {@link #getNearestNeighboursGeneric(int, PrimitiveTypeProvider, String, Class, ReadableQueryConfig)}
   */
  default <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] query, String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
    return getNearestNeighboursGeneric(k, new FloatArrayTypeProvider(query), column, distanceElementClass, config);
  }

  /**
   * * Finds the {@code k}-nearest neighbours of the given {@code queryProvider} in {@code column} using the provided distance function in {@code config}. {@code ScoreElementClass} defines the specific type of {@link DistanceElement} to be created internally and returned by this method.
   *
   * @param k maximum number of results
   * @param queryProvider query vector
   * @param column feature column to do the search
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param config query config
   * @param <E> type of the {@link DistanceElement}
   * @return a list of elements with their distance
   */
  default <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k,
      PrimitiveTypeProvider queryProvider, String column, Class<E> distanceElementClass,
      ReadableQueryConfig config) {
    if (queryProvider.getType().equals(ProviderDataType.FLOAT_ARRAY) || queryProvider.getType()
        .equals(ProviderDataType.INT_ARRAY)) {
      //Default-implementation for backwards compatibility.
      return getNearestNeighboursGeneric(k, PrimitiveTypeProvider.getSafeFloatArray(queryProvider), column,
          distanceElementClass, config);
    }
    LogManager.getLogger().error("{} does not support other queries than float-arrays.",
        this.getClass().getSimpleName());
    throw new UnsupportedOperationException();
  }

  /**
   * Performs a batched kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector in the provided list and returns the union of the results for every query.
   *
   * @param k The number k vectors to return per query.
   * @param vectors The list of vectors to use.
   * @param column The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs The query configuration, which may contain distance definitions or query-hints.
   * @param <T> The type T of the resulting <T> type of the {@link DistanceElement}.
   * @return List of results.
   */
  <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors,
      String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs);

  List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config);

  /**
   * SELECT 'vectorname' from entity where 'fieldname' = 'value'
   */
  List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName);

  /**
   * for legacy support, takes the float[] method by default
   */
  default List<PrimitiveTypeProvider> getFeatureVectorsGeneric(String fieldName, PrimitiveTypeProvider value,
      String vectorName) {
    return getFeatureVectors(fieldName, value, vectorName).stream().map(FloatArrayTypeProvider::new)
        .collect(Collectors.toList());
  }

  default List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, PrimitiveTypeProvider value) {
    return getRows(fieldName, Collections.singleton(value));
  }

  default List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, PrimitiveTypeProvider... values) {
    return getRows(fieldName, Arrays.asList(values));
  }

  /**
   * SELECT * where fieldName IN (values)
   */
  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values);

  /**
   * Conversion to PrimitiveTypeProviders is expensive so feel free to use & implement extension for generic objects
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values) {
    return getRows(fieldName, values.stream().map(StringTypeProvider::new).collect(Collectors.toList()));
  }

  /**
   * Performs a fulltext search with multiple query terms. That is, the storage engine is tasked to lookup for entries in the provided fields that match the provided query terms.
   *
   * @param rows The number of rows that should be returned.
   * @param fieldname The field that should be used for lookup.
   * @param terms The query terms. Individual terms will be connected by a logical OR.
   * @return List of rows that math the fulltext search.
   */
  List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig,
      String... terms);

  /**
   * {@link #getRows(String, RelationalOperator, Iterable)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator,
      PrimitiveTypeProvider value) {
    return getRows(fieldName, operator, Collections.singleton(value));
  }

  /**
   * Performs a boolean lookup on a specified field  and retrieves the rows that match the specified condition.
   *
   * i.e. SELECT * from WHERE A <Operator> B
   *
   * @param fieldName The name of the database field .
   * @param operator The {@link RelationalOperator} that should be used for comparison.
   * @param values The values the field should be compared to.
   * @return List of rows (one row is represented by one Map of the field ames and the data contained in the field).
   */
  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator,
      Iterable<PrimitiveTypeProvider> values);

  /**
   * Performs a boolean lookup based on multiple conditions, linked with AND. Each element of the list specifies one of the conditions - left middle right, i.e. id IN (1, 5, 7)
   *  @param conditions conditions which will be linked by AND
   * @param identifier column upon which the retain operation will be performed if the database layer does not support compound boolean retrieval.
   * @param projection Which columns shall be selected
   * @param qc
   */
  default List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig qc) {
    HashMap<String, Map<String, PrimitiveTypeProvider>> relevant = new HashMap<>();
    for (Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> condition : conditions) {
      List<Map<String, PrimitiveTypeProvider>> rows = this.getRows(condition.getLeft(), condition.getMiddle(), condition.getRight());
      if (rows.isEmpty()) {
        return Collections.emptyList();
      }
      Set<String> ids = rows.stream().map(x -> x.get(identifier).getString())
          .collect(Collectors.toSet());

      if (relevant.size() == 0) {
        rows.forEach(map -> relevant.put(map.get(identifier).getString(), map));
      } else {
        relevant.keySet().retainAll(ids);
      }
    }
    if (relevant.isEmpty()) {
      return Collections.emptyList();
    }

    return new ArrayList<>(relevant.values());
  }

  /**
   * SELECT DISTINCT column from table
   */
  default List<PrimitiveTypeProvider> getUniqueValues(String column) {
    Set<PrimitiveTypeProvider> uniques = new HashSet<>();
    getAll().forEach(row -> row.forEach((key, value) -> {
      if (key.equals(column)) {
        uniques.add(value);
      }
    }));
    return Lists.newArrayList(uniques);
  }

  /**
   * SELECT column from the table. Be careful with large entities
   */
  List<PrimitiveTypeProvider> getAll(String column);

  /**
   * Get all rows from all tables
   */
  List<Map<String, PrimitiveTypeProvider>> getAll();

  boolean existsEntity(String name);

  /**
   * Healthcheck. Returns false if something is wrong
   */
  boolean ping();
}
