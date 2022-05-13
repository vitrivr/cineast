package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.DOMAIN_COL_NAME;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.KEY_COL_NAME;
import static org.vitrivr.cineast.core.util.DBQueryIDGenerator.generateQueryID;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;
import org.vitrivr.cottontail.client.iterators.Tuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.client.language.basics.Direction;
import org.vitrivr.cottontail.client.language.basics.Distances;
import org.vitrivr.cottontail.client.language.basics.predicate.And;
import org.vitrivr.cottontail.client.language.basics.predicate.Expression;
import org.vitrivr.cottontail.client.language.basics.predicate.Or;
import org.vitrivr.cottontail.client.language.basics.predicate.Predicate;
import org.vitrivr.cottontail.client.language.ddl.AboutEntity;
import org.vitrivr.cottontail.client.language.dql.Query;

public final class CottontailSelector implements DBSelector {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Internal reference to the {@link CottontailWrapper} used by this {@link CottontailSelector}.
   */
  private final CottontailWrapper cottontail;

  /**
   * The fully qualified name of the entity handled by this {@link CottontailSelector}.
   */
  private String fqn;

  public CottontailSelector(CottontailWrapper wrapper) {
    this.cottontail = wrapper;
  }

  private static List<Map<String, PrimitiveTypeProvider>> processResults(TupleIterator results, Map<String, String> mappings) {
    final List<Map<String, PrimitiveTypeProvider>> _return = new LinkedList<>();
    final StopWatch watch = StopWatch.createStarted();
    final Collection<String> columns = results.getSimpleNames();
    while (results.hasNext()) {
      final Tuple t = results.next();
      final Map<String, PrimitiveTypeProvider> map = new HashMap<>(results.getNumberOfColumns());
      for (String c : columns) {
        map.put(mappings.getOrDefault(c, c), PrimitiveTypeProvider.fromObject(t.get(c)));
      }
      _return.add(map);
    }
    LOGGER.trace("Processed {} results in {} ms", _return.size(), watch.getTime(TimeUnit.MILLISECONDS));
    return _return;
  }

  /**
   * Converts a {@link TupleIterator} response generated by Cottontail DB into a {@link List} {@link Map}s that contain the results.
   *
   * @param results {@link TupleIterator} to gather the results from.
   * @return {@link List} of {@link Map}s that contains the results.
   */
  private static List<Map<String, PrimitiveTypeProvider>> processResults(TupleIterator results) {
    return processResults(results, new HashMap<>());
  }

  /**
   * Converts a {@link TupleIterator} response generated by Cottontail DB into a {@link List} of {@link DistanceElement}s.
   *
   * @param response             {@link TupleIterator} to gather the results from.
   * @param distanceElementClass The type of {@link DistanceElement} to create.
   * @return {@link List} of {@link DistanceElement}s.
   */
  private static <T extends DistanceElement> List<T> handleNearestNeighbourResponse(TupleIterator response, Class<? extends T> distanceElementClass) {
    final List<T> result = new LinkedList<>();
    while (response.hasNext()) {
      try {
        final Tuple t = response.next();
        final String id = t.asString(GENERIC_ID_COLUMN_QUALIFIER);

        double distance = Double.POSITIVE_INFINITY;

        switch (t.type(DB_DISTANCE_VALUE_QUALIFIER)) {

          case BOOLEAN: {
            distance = Boolean.TRUE.equals(t.asBoolean(DB_DISTANCE_VALUE_QUALIFIER)) ? 1d : 0d;
            break;
          }
          case BYTE: {
            distance = t.asByte(DB_DISTANCE_VALUE_QUALIFIER);
            break;
          }
          case SHORT:
            distance = t.asShort(DB_DISTANCE_VALUE_QUALIFIER);
            break;
          case INTEGER:
            distance = t.asInt(DB_DISTANCE_VALUE_QUALIFIER);
            break;
          case LONG:
            distance = t.asLong(DB_DISTANCE_VALUE_QUALIFIER);
            break;
          case FLOAT:
            distance = t.asFloat(DB_DISTANCE_VALUE_QUALIFIER);
            break;
          case DOUBLE:
            distance = t.asDouble(DB_DISTANCE_VALUE_QUALIFIER);
            break;

        }

        T e = DistanceElement.create(distanceElementClass, id, distance);
        result.add(e);
      } catch (NullPointerException e) {
        LOGGER.warn("Encountered null entry (id, distance) is nearest neighbor search response!");
      }
    }
    return result;
  }

  /**
   * Converts a Cineast {@link Distance} into the corresponding Cottontail DB representation.
   *
   * @param distance {@link Distance} to convert.
   * @return {@link String} Name of Cottontail DB distance.
   */
  private static Distances toDistance(Distance distance) {
    switch (distance) {
      case manhattan:
        return Distances.L1;
      case euclidean:
        return Distances.L2;
      case squaredeuclidean:
        return Distances.L2SQUARED;
      case chisquared:
        return Distances.CHISQUARED;
      case cosine:
        return Distances.COSINE;
      case haversine:
        return Distances.HAVERSINE;
      default:
        LOGGER.error("distance '{}' not supported by cottontail", distance);
        throw new IllegalArgumentException("Distance '" + distance + "' not supported by Cottontail DB.");
    }
  }

  /**
   * Converts a Cineast {@link RelationalOperator} into the corresponding Cottontail DB representation.
   *
   * @param op {@link RelationalOperator} to convert.
   * @return {@link String} Name of Cottontail DB distance.
   */
  private static String toOperator(RelationalOperator op) {
    switch (op) {
      case EQ:
        return "=";
      case NEQ:
        return "!=";
      case GEQ:
        return ">=";
      case LEQ:
        return "<=";
      case GREATER:
        return ">";
      case LESS:
        return "<";
      case BETWEEN:
        return "BETWEEN";
      case LIKE:
        return "LIKE";
      case NLIKE:
        return "NOT LIKE";
      case MATCH:
        return "MATCH";
      case ISNULL:
        return "IS NULL";
      case ISNOTNULL:
        return "IS NOT NULL";
      case IN:
        return "IN";
      default:
        throw new IllegalArgumentException("Operator '" + op + "' not supported by Cottontail DB.");
    }
  }

  @Override
  public boolean open(String name) {
    this.fqn = this.cottontail.fqnInput(name);
    return true;
  }

  public CottontailWrapper getWrapper() {
    return this.cottontail;
  }

  @Override
  public void close() { /* No op. */ }

  /**
   * if {@link ReadableQueryConfig#getRelevantSegmentIds()} is null, the where-clause will be left empty
   */
  @Override
  public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] vector, String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
    final Query query = knn(k, vector, column, config);
    try {
      return handleNearestNeighbourResponse(this.cottontail.client.query(query), distanceElementClass);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getNearestNeighboursGeneric(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<SegmentDistanceElement> getFarthestNeighboursGeneric(int k, PrimitiveTypeProvider queryProvider, String column, Class<SegmentDistanceElement> distanceElementClass, ReadableQueryConfig config) {
    if (queryProvider.getType().equals(ProviderDataType.FLOAT_ARRAY) || queryProvider.getType().equals(ProviderDataType.INT_ARRAY)) {
      //Default-implementation for backwards compatibility.
      var vector = PrimitiveTypeProvider.getSafeFloatArray(queryProvider);
      final var query = kn(k, vector, column, config, Direction.DESC, "id");
      try {
        return handleNearestNeighbourResponse(this.cottontail.client.query(query), distanceElementClass);
      } catch (StatusRuntimeException e) {
        LOGGER.warn("Error occurred during query execution in getNearestNeighboursGeneric(): {}", e.getMessage());
        return new ArrayList<>(0);
      }
    }
    throw new UnsupportedOperationException("other types than float vectors are not supported for farthest neighbors");
  }

  @Override
  public <E extends DistanceElement> List<E> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<E> distanceElementClass, List<ReadableQueryConfig> configs) {
    return new ArrayList<>(0); /* TODO. */
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
    final Query query = knn(k, vector, column, config, "*");
    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getNearestNeighbourRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName, ReadableQueryConfig queryConfig) {
    final Query query = new Query(this.fqn).select(vectorName, null).where(new Expression(fieldName, "==", value.toObject())).queryId(generateQueryID("get-fv", queryConfig));
    try {
      final TupleIterator results = this.cottontail.client.query(query);
      final List<float[]> _return = new LinkedList<>();
      while (results.hasNext()) {
        final Tuple t = results.next();
        _return.add(t.asFloatVector(vectorName));
      }
      return _return;
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }


  @Override
  public List<PrimitiveTypeProvider> getFeatureVectorsGeneric(String fieldName, PrimitiveTypeProvider value, String vectorName, ReadableQueryConfig qc) {
    final Query query = new Query(this.fqn).select(vectorName, null).where(new Expression(fieldName, "==", value.toObject())).queryId(generateQueryID("get-fv-gen", qc));
    try {
      return toSingleCol(this.cottontail.client.query(query), vectorName);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFeatureVectorsGeneric(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
    return getRowsHelper(fieldName, "IN", mapped, "get-rows-in-iterable");
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values) {
    final Object[] mapped = values.toArray();
    return getRowsHelper(fieldName, "IN", mapped, "get-rows-stringlist");
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values, String dbQueryID) {
    final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
    return getRowsHelper(fieldName, "IN", mapped, dbQueryID);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values, String dbQueryID) {
    final Object[] mapped = values.toArray();
    return getRowsHelper(fieldName, "IN", mapped, dbQueryID);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
    final String op = toOperator(operator);
    return getRowsHelper(fieldName, op, mapped, "get-rows-" + op + "-iterable");
  }

  private List<Map<String, PrimitiveTypeProvider>> getRowsHelper(String fieldName, String op, Object[] mapped, String dbQueryID) {
    if (op.equals("IN") && mapped.length == 0) {
      LOGGER.debug("empty in-clause, not executing query {}", dbQueryID);
      return new ArrayList<>(0);
    }
    final Query query = new Query(this.fqn).select("*", null).where(new Expression(fieldName, op, mapped)).queryId(dbQueryID);
    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
    /* Prepare plain query. */
    final String predicate = Arrays.stream(terms).map(String::trim).collect(Collectors.joining(" OR "));
    final Query query = new Query(this.fqn)
        .select("*", null)
        .fulltext(fieldname, predicate, DB_DISTANCE_VALUE_QUALIFIER)
        .queryId(generateQueryID("ft-rows", queryConfig));


    /* Process predicates. */
    if (queryConfig != null && !queryConfig.getRelevantSegmentIds().isEmpty()) {
      final Set<String> relevant = queryConfig.getRelevantSegmentIds();
      final Expression segmentIds = new Expression(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray());
      query.where(new And(segmentIds, new Expression(DB_DISTANCE_VALUE_QUALIFIER, ">", 0.0)));
    } else {
      query.where(new Expression(DB_DISTANCE_VALUE_QUALIFIER, ">", 0.0));
    }

    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFulltextRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig qc) {
    /* Prepare plain query. */
    final Query query = new Query(this.fqn).queryId(generateQueryID("get-rows-and", qc));
    if (projection.isEmpty()) {
      query.select("*", null);
    } else {
      for (String p : projection) {
        query.select(p, null);
      }
    }

    /* Process predicates. */
    final List<Predicate> atomics = conditions.stream().map(c -> {
      final String op = toOperator(c.getMiddle());
      return new Expression(c.getLeft(), op, c.getRight().stream().map(PrimitiveTypeProvider::toObject).toArray());
    }).collect(Collectors.toList());

    /*  */
    final Optional<Predicate> predicates = atomics.stream().reduce(And::new);
    if (qc != null && !qc.getRelevantSegmentIds().isEmpty()) {
      final Set<String> relevant = qc.getRelevantSegmentIds();
      final Expression segmentIds = new Expression(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray());
      if (predicates.isPresent()) {
        query.where(new And(segmentIds, predicates.get()));
      } else {
        query.where(segmentIds);
      }
    } else {
      predicates.ifPresent(query::where);
    }

    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRowsAND(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getMetadataBySpec(List<MetadataAccessSpecification> spec, String dbQueryID) {
    final Query query = new Query(this.fqn).select("*", null).queryId(dbQueryID);
    final Optional<Predicate> predicates = generateQueryFromMetadataSpec(spec);
    predicates.ifPresent(query::where);
    return processResults(this.cottontail.client.query(query));
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getMetadataByIdAndSpec(List<String> ids, List<MetadataAccessSpecification> spec, String idColName, String dbQueryID) {
    final Query query = new Query(this.fqn).select("*", null).queryId(dbQueryID == null ? "md-id-spec" : dbQueryID);
    final Optional<Predicate> predicates = generateQueryFromMetadataSpec(spec);
    final Expression segmentIds = new Expression(idColName, "IN", ids.toArray());
    if (predicates.isPresent()) {
      query.where(new And(segmentIds, predicates.get()));
    } else {
      query.where(segmentIds);
    }
    return processResults(this.cottontail.client.query(query));
  }

  public Optional<Predicate> generateQueryFromMetadataSpec(List<MetadataAccessSpecification> spec) {
    final List<Optional<Predicate>> atomics = spec.stream().map(s -> {
      List<Predicate> singleSpecPredicates = new ArrayList<>();
      if (!s.domain.isEmpty() && !s.domain.equals("*")) {
        singleSpecPredicates.add(new Expression(DOMAIN_COL_NAME, "=", s.domain));
      }
      if (!s.key.isEmpty() && !s.key.equals("*")) {
        singleSpecPredicates.add(new Expression(KEY_COL_NAME, "=", s.key));
      }
      return singleSpecPredicates.stream().reduce(And::new);
    }).collect(Collectors.toList());

    final Optional<Optional<Predicate>> reduce = atomics.stream().reduce((res, el) -> {
      if (res.isEmpty() && el.isEmpty()) {
        return Optional.empty();
      }
      if (res.isEmpty()) {
        return el;
      }
      if (el.isEmpty()) {
        return res;
      }
      return Optional.of(new Or(res.get(), el.get()));
    });
    return reduce.orElseGet(Optional::empty);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    final Query query = new Query(this.fqn).select(column, null).queryId(generateQueryID("all-" + column));
    try {
      return toSingleCol(this.cottontail.client.query(query), column);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll(List<String> columns, int limit) {
    final Query query = new Query(this.fqn);
    query.queryId(generateQueryID("all-cols-limit-" + limit));
    for (String c : columns) {
      query.select(c, null);
    }
    if (limit > 0) {
      query.limit(limit);
    }
    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<PrimitiveTypeProvider> getUniqueValues(String column) {
    final Query query = new Query(this.fqn).distinct(column, null).
        queryId(generateQueryID("unique-" + column));
    try {
      return toSingleCol(this.cottontail.client.query(query), column);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getUniqueValues(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  public Map<String, Integer> countDistinctValues(String column) {
    final Query query = new Query(this.fqn).select(column, null)
        .queryId(generateQueryID("count-distinct-" + column));
    final Map<String, Integer> count = new HashMap<>();
    try {
      final TupleIterator results = this.cottontail.client.query(query);
      while (results.hasNext()) {
        final Tuple t = results.next();
        count.merge(t.asString(column), 1, (old, one) -> old + 1);
      }
      return count;
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in countDistinctValues(): {}", e.getMessage());
      return new HashMap<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    final Query query = new Query(this.fqn).select("*", null)
        .queryId(generateQueryID("get-all-" + this.fqn));
    try {
      return processResults(this.cottontail.client.query(query));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll(String order, int skip, int limit) {
    final Query query = new Query(this.fqn).select("*", null)
        .queryId(generateQueryID("get-all-order-skip-limit-" + this.fqn))
        .order(order, Direction.ASC)
        .skip(skip)
        .limit(limit);
    return processResults(this.cottontail.client.query(query));
  }

  @Override
  public int rowCount() {
    final Query query = new Query(this.fqn).count().queryId("count-star-" + this.fqn);
    return Math.toIntExact(this.cottontail.client.query(query).next().asLong(0));
  }

  @Override
  public boolean existsEntity(String name) {
    final AboutEntity about = new AboutEntity(this.cottontail.fqnInput(name));
    try (final TupleIterator results = this.cottontail.client.about(about)) {
      return results.hasNext();
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() != Status.Code.NOT_FOUND) {
        LOGGER.error("Error occurred during query execution in existsEntity(): {}!", e.getMessage());
      }
      return false;
    } catch (Exception e) {
      LOGGER.error("Error occurred during query execution in existsEntity(): {}!", e.getMessage());
      return false;
    }
  }

  @Override
  public boolean ping() {
    try {
      return this.cottontail.client.ping();
    } catch (StatusRuntimeException e) {
      return false;
    }
  }

  /**
   * Creates and returns a basic {@link Query} object for the given kNN parameters.
   *
   * @param k      The k parameter used for kNN
   * @param vector The query vector (= float array).
   * @param column The name of the column that should be queried.
   * @param config The {@link ReadableQueryConfig} with additional parameters.
   * @return {@link Query}
   */
  private Query knn(int k, float[] vector, String column, ReadableQueryConfig config) {
    return knn(k, vector, column, config, GENERIC_ID_COLUMN_QUALIFIER);
  }

  /**
   * Creates and returns a basic {@link Query} object for the given kNN parameters.
   *
   * @param k      The k parameter used for kNN
   * @param vector The query vector (= float array).
   * @param column The name of the column that should be queried.
   * @param config The {@link ReadableQueryConfig} with additional parameters.
   * @param select which rows should be selected
   * @return {@link Query}
   */
  private Query knn(int k, float[] vector, String column, ReadableQueryConfig config, String... select) {
    return kn(k, vector, column, config, Direction.ASC, select);
  }

  /**
   * Creates and returns a basic {@link Query} object for the given k(f|n)N parameters.
   *
   * @param k      The k parameter used for the search
   * @param vector The query vector (= float array).
   * @param column The name of the column that should be queried.
   * @param config The {@link ReadableQueryConfig} with additional parameters.
   * @param config ASC for knn DESC for farthest neighbor
   * @param select which rows should be selected
   * @return {@link Query}
   */
  private Query kn(int k, float[] vector, String column, ReadableQueryConfig config, Direction direction, String... select) {
    final Set<String> relevant = config.getRelevantSegmentIds();
    final Distances distance = toDistance(config.getDistance().orElse(Distance.manhattan));
    final Query query = new Query(this.fqn)
        .distance(column, vector, distance, DB_DISTANCE_VALUE_QUALIFIER)
        .order(DB_DISTANCE_VALUE_QUALIFIER, direction)
        .limit(k)
        .queryId(generateQueryID("kfn", config));

    for (String s : select) {
      query.select(s, null);
    }


    /* Add relevant segments (optional). */
    if (!relevant.isEmpty()) {
      query.where(new Expression(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray()));
    }

    return query;
  }

  /**
   * Converts a {@link TupleIterator} response generated by Cottontail DB into a {@link List} {@link PrimitiveTypeProvider}s that contain the results of a single column.
   *
   * @param results {@link TupleIterator} to gather the results from.
   * @param colName The name of the column that should be collected.
   * @return {@link List} of {@link Map}s that contains the results.
   */
  private List<PrimitiveTypeProvider> toSingleCol(TupleIterator results, String colName) {
    final List<PrimitiveTypeProvider> _return = new LinkedList<>();
    while (results.hasNext()) {
      final Tuple t = results.next();
      _return.add(PrimitiveTypeProvider.fromObject(t.get(colName)));
    }
    return _return;
  }
}
