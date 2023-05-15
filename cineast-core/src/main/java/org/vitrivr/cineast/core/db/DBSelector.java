package org.vitrivr.cineast.core.db;

import static org.vitrivr.cineast.core.util.CineastConstants.DOMAIN_COL_NAME;
import static org.vitrivr.cineast.core.util.CineastConstants.KEY_COL_NAME;

import com.google.common.collect.Lists;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.compare.ObjectToStringComparator;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

public interface DBSelector extends Closeable {

  Logger LOGGER = LogManager.getLogger();

  /**
   * Tells this selector to use a specific entity. This is intended to be called once per selector after it is instantiated, calling it multiple times may cause issues depending on the underlying implementation.
   */
  boolean open(String name);

  /**
   * Closes this selector and all associated resources. Calling any method afterwards may cause issues.
   */
  void close();

  /**
   * Convenience-wrapper to query with float-arrays {@link #getNearestNeighboursGeneric(int, PrimitiveTypeProvider, String, Class, ReadableQueryConfig)}
   */
  default <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] query, String column, Class<E> distanceElementClass, ReadableQueryConfig queryConfig) {
    return getNearestNeighboursGeneric(k, new FloatArrayTypeProvider(query), column, distanceElementClass, queryConfig);
  }

  /**
   * * Finds the {@code k}-nearest neighbours of the given {@code queryProvider} in {@code column} using the provided distance function in {@code queryConfig}. {@code ScoreElementClass} defines the specific type of {@link DistanceElement} to be created internally and returned by this method.
   *
   * @param k                    maximum number of results
   * @param queryProvider        query vector
   * @param column               feature column to do the nns on
   * @param distanceElementClass class of the {@link DistanceElement} type (e.g., {@link SegmentDistanceElement}
   * @param <T>                  type of the {@link DistanceElement}
   * @param queryConfig          query config
   * @return a list of ids with their distance
   */
  default <T extends DistanceElement> List<T> getNearestNeighboursGeneric(int k, PrimitiveTypeProvider queryProvider, String column, Class<T> distanceElementClass, ReadableQueryConfig queryConfig) {
    if (queryProvider.getType().equals(ProviderDataType.FLOAT_ARRAY) || queryProvider.getType().equals(ProviderDataType.INT_ARRAY)) {
      //Default-implementation for backwards compatibility.
      return getNearestNeighboursGeneric(k, PrimitiveTypeProvider.getSafeFloatArray(queryProvider), column, distanceElementClass, queryConfig);
    }
    LogManager.getLogger().error("{} does not support other queries than float-arrays.", this.getClass().getSimpleName());
    throw new UnsupportedOperationException();
  }

  /**
   * Performs a batched kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector in the provided list and returns the union of the results for every query.
   *
   * @param k                    maximum number of results
   * @param vectors              list of query vectors
   * @param column               feature column to do the nns on
   * @param distanceElementClass class of the {@link DistanceElement} type (e.g., {@link SegmentDistanceElement}
   * @param <T>                  The type T of the resulting <T> type of the {@link DistanceElement}.
   * @param queryConfigs         query configs
   * @return a list of ids with their distance
   */
  <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> queryConfigs);

  /**
   * In contrast to {@link #getNearestNeighboursGeneric(int, float[], String, Class, ReadableQueryConfig)}, this method returns all columns per result row
   */
  List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig queryConfig);

  /**
   * SELECT 'featureColName' from entity where 'column' = 'value'
   */
  List<PrimitiveTypeProvider> getFeatures(String column, PrimitiveTypeProvider value, String featureColName, ReadableQueryConfig queryConfig);

  /**
   * {@link #getRows(String, Iterable, String)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String column, PrimitiveTypeProvider value) {
    return getRows(column, Collections.singleton(value), null);
  }

  /**
   * {@link #getRows(String, Iterable, String)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String column, Iterable<PrimitiveTypeProvider> values) {
    return getRows(column, values, null);
  }

  /**
   * SELECT * where column IN (values)
   */
  List<Map<String, PrimitiveTypeProvider>> getRows(String column, Iterable<PrimitiveTypeProvider> values, String dbQueryId);

  /**
   * Conversion to PrimitiveTypeProviders is expensive so underlying classes should feel free to override if they wish to optimize for performance
   * <p>
   * {@link #getRows(String, Iterable, String)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String column, List<String> values) {
    return getRows(column, values.stream().map(StringTypeProvider::new).collect(Collectors.toList()), null);
  }

  /**
   * Conversion to PrimitiveTypeProviders is expensive so underlying classes should feel free to override if they wish to optimize for performance
   * <p>
   * {@link #getRows(String, Iterable, String)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String column, List<String> values, String dbQueryId) {
    return getRows(column, values.stream().map(StringTypeProvider::new).collect(Collectors.toList()), null);
  }

  /**
   * Performs a fulltext search with multiple query terms. That is, the storage engine is tasked to lookup for entries in the provided fields that match the provided query terms.
   *
   * @param rows        The number of rows that should be returned.
   * @param column      The field that should be used for lookup.
   * @param queryConfig query config
   * @param terms       The query terms. Individual terms will be connected by a logical OR.
   * @return List of rows that math the fulltext search.
   */
  List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String column, ReadableQueryConfig queryConfig, String... terms);

  /**
   * {@link #getRows(String, RelationalOperator, Iterable, ReadableQueryConfig)}
   */
  default List<Map<String, PrimitiveTypeProvider>> getRows(String column, RelationalOperator operator, PrimitiveTypeProvider value, ReadableQueryConfig queryConfig) {
    return getRows(column, operator, Collections.singleton(value), queryConfig);
  }

  /**
   * Performs a boolean lookup on a specified field  and retrieves the rows that match the specified condition.
   * <p>
   * i.e. SELECT * from WHERE A <Operator> B
   *
   * @param column      The name of the database field .
   * @param operator    The {@link RelationalOperator} that should be used for comparison.
   * @param values      The values the field should be compared to.
   * @param queryConfig query config
   * @return List of rows (one row is represented by one Map of the field ames and the data contained in the field).
   */
  List<Map<String, PrimitiveTypeProvider>> getRows(String column, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values, ReadableQueryConfig queryConfig);

  /**
   * Performs a boolean lookup based on multiple conditions, linked with AND. Each element of the list specifies one of the conditions - left middle right, i.e. id IN (1, 5, 7)
   *
   * @param conditions  conditions which will be linked by AND
   * @param identifier  column upon which the retain operation will be performed if the database layer does not support compound boolean retrieval.
   * @param projection  Which columns shall be selected
   * @param queryConfig query config
   */
  default List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig queryConfig) {
    HashMap<String, Map<String, PrimitiveTypeProvider>> relevant = new HashMap<>();
    for (Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> condition : conditions) {
      List<Map<String, PrimitiveTypeProvider>> rows = this.getRows(condition.getLeft(), condition.getMiddle(), condition.getRight(), queryConfig);
      if (rows.isEmpty()) {
        return Collections.emptyList();
      }
      Set<String> ids = rows.stream().map(x -> x.get(identifier).getString()).collect(Collectors.toSet());

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
   * counts how many times each element appears per value in a given column. This can be useful for example to debug duplicates or count occurences of tags
   */
  default Map<String, Integer> countDistinctValues(String column) {
    Map<String, Integer> count = new HashMap<>();
    this.getAll(Collections.singletonList(column), -1).forEach(el -> count.compute(el.get(column).getString(), (k, v) -> v == null ? 1 : v++));
    return count;
  }

  /**
   * Returns all available metadata based on the specification.
   */
  default List<Map<String, PrimitiveTypeProvider>> getMetadataBySpec(List<MetadataAccessSpecification> spec, String dbQueryId, ReadableQueryConfig queryConfig) {
    return this.getMetadataByIdAndSpec(null, spec, null, dbQueryId, queryConfig);
  }

  /**
   * Retrieves Metadata based on ids, access specification and other parameters
   *
   * @param ids         ids for which to fetch metadata
   * @param spec        which metadata should be fetched
   * @param idColName   the name of the column which the id refers to. Can be null, in which case the default behavior is used
   * @param dbQueryId   query identifier. Can be null
   * @param queryConfig query config
   */
  default List<Map<String, PrimitiveTypeProvider>> getMetadataByIdAndSpec(List<String> ids, List<MetadataAccessSpecification> spec, String idColName, String dbQueryId, ReadableQueryConfig queryConfig) {
    LOGGER.trace("fetching metadata with spec, dbQueryId {}", dbQueryId);
    return getAll().stream().filter(tuple -> {
      // check if there are any elements of the specification which do not work
      if (spec.stream().noneMatch(el -> {
        if (ids != null) {
          if (!tuple.containsKey(idColName)) {
            return false;
          }
          // check if this matches with one of the given ids
          if (ids.stream().noneMatch(id -> id.equals(tuple.get(idColName).getString()))) {
            return false;
          }
        }
        // at this point, if there is an id list, the element is within that list
        if (!el.domain().equals("*")) {
          if (!tuple.containsKey(DOMAIN_COL_NAME)) {
            return false;
          }
          if (!tuple.get(DOMAIN_COL_NAME).getString().equals(el.domain())) {
            return false;
          }
        }
        // at this point, if a domain is specified, the element matches that domain
        if (!el.key().equals("*")) {
          if (!tuple.containsKey(KEY_COL_NAME)) {
            return false;
          }
          if (!tuple.get(KEY_COL_NAME).getString().equals(el.key())) {
            return false;
          }
        }
        //if we are here, it means the spec is good w.r.t. to the element - this element should not be blocked
        return true;
      })) {
        //if there are any matches, return false to filter the element
        return false;
      }
      // if the spec matches, this element is fine
      return true;
    }).collect(Collectors.toList());
  }

  /**
   * SELECT columns from the table. Be careful with large entities (SELECT columns FROM table)
   *
   * @param limit if <= 0, parameter is ignored
   */
  default List<Map<String, PrimitiveTypeProvider>> getAll(List<String> columns, int limit) {
    List<Map<String, PrimitiveTypeProvider>> collect = getAll().stream().map(el -> {
      Map<String, PrimitiveTypeProvider> m = new HashMap<>();
      el.forEach((k, v) -> {
        if (columns.contains(k)) {
          m.put(k, v);
        }
      });
      return m;
    }).collect(Collectors.toList());
    if (limit > 0) {
      return collect.subList(0, limit);
    }
    return collect;
  }

  /**
   * SELECT * FROM entity ORDER BY order ASC LIMIT limit SKIP skip
   * <br>
   * skip is also sometimes called offset. This is horribly inefficient in the default implementation, as it serializes to string and then sorts.
   */
  default List<Map<String, PrimitiveTypeProvider>> getAll(String order, int skip, int limit) {
    return getAll().stream().sorted((o1, o2) -> ObjectToStringComparator.INSTANCE.compare(o1.get(order), o2.get(order))).skip(skip).limit(limit).collect(Collectors.toList());
  }

  /**
   * Get all rows from the entities (SELECT * FROM table)
   */
  List<Map<String, PrimitiveTypeProvider>> getAll();

  /**
   * SELECT count(*) FROM table
   */
  default int rowCount() {
    return getAll().size();
  }

  boolean existsEntity(String name);

  /**
   * Healthcheck. Returns false if something is wrong
   */
  boolean ping();

}
