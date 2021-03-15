package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import io.grpc.StatusRuntimeException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cottontail.client.TupleIterator;
import org.vitrivr.cottontail.client.language.ddl.AboutEntity;
import org.vitrivr.cottontail.client.language.dql.Query;
import org.vitrivr.cottontail.client.language.extensions.And;
import org.vitrivr.cottontail.client.language.extensions.Literal;
import org.vitrivr.cottontail.client.language.extensions.Or;
import org.vitrivr.cottontail.client.language.extensions.Predicate;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Knn;

public final class CottontailSelector implements DBSelector {

  private static final Logger LOGGER = LogManager.getLogger();

  /** Internal reference to the {@link CottontailWrapper} used by this {@link CottontailSelector}. */
  private final CottontailWrapper cottontail;

  /** The fully qualified name of the entity handled by this {@link CottontailSelector}. */
  private String fqn;

  public CottontailSelector(CottontailWrapper wrapper) {
    this.cottontail = wrapper;
  }

  @Override
  public boolean open(String name) {
    this.fqn = this.cottontail.fqn(name);
    return true;
  }

  @Override
  public boolean close() {
    this.cottontail.close();
    return true;
  }

  /**
   * if {@link ReadableQueryConfig#getRelevantSegmentIds()} is null, the where-clause will be left empty
   */
  @Override
  public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k, float[] vector, String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
    final Query query = knn(k, vector, column, config).select(GENERIC_ID_COLUMN_QUALIFIER, DB_DISTANCE_VALUE_QUALIFIER);
    try {
      return handleNearestNeighbourResponse(this.cottontail.client.query(query, null), distanceElementClass);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getNearestNeighboursGeneric(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public <E extends DistanceElement> List<E> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<E> distanceElementClass, List<ReadableQueryConfig> configs) {
    return new ArrayList<>(0); /* TODO. */
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config) {
    final Query query = knn(k, vector, column, config).select("*");
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getNearestNeighbourRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    final Query query = new Query(this.fqn).select(vectorName).where(new Literal(fieldName, "==", value.toObject()));
    try {
      final TupleIterator results = this.cottontail.client.query(query, null);
      final List<float[]> _return = new LinkedList<>();
      while (results.hasNext()) {
        final TupleIterator.Tuple t = results.next();
        _return.add(t.asFloatVector(vectorName));
      }
      return _return;
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFeatureVectors(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<PrimitiveTypeProvider> getFeatureVectorsGeneric(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    final Query query = new Query(this.fqn).select(vectorName).where(new Literal(fieldName, "==", value.toObject()));
    try {
      return toSingleCol(this.cottontail.client.query(query, null), vectorName);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFeatureVectorsGeneric(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<PrimitiveTypeProvider> values) {
    final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
    final Query query = new Query(this.fqn).select("*").where(new Literal(fieldName, "IN", mapped));
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, List<String> values) {
    final Object[] mapped = values.toArray();
    final Query query = new Query(this.fqn).select("*").where(new Literal(fieldName, "IN", mapped));
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    final Object[] mapped = StreamSupport.stream(values.spliterator(), false).map(PrimitiveTypeProvider::toObject).toArray();
    final Pair<String,Boolean> op = toOperator(operator);
    final Query query = new Query(this.fqn).select("*").where(new Literal(fieldName, op.first, mapped, op.second));
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig, String... terms) {
    /* Prepare plain query. */
    final Query query = new Query(this.fqn).select(
        new kotlin.Pair<String, String>(GENERIC_ID_COLUMN_QUALIFIER, null),
        new kotlin.Pair<String, String>("score", "ap_score")
    );

    /* Process predicates. */
    final List<Predicate> atomics = Arrays.stream(terms).map(t -> new Literal(fieldname, "MATCH", t)).collect(Collectors.toList());
    final Optional<Predicate> predicates = atomics.stream().reduce(Or::new);
    if (queryConfig != null && !queryConfig.getRelevantSegmentIds().isEmpty()) {
      final Set<String> relevant = queryConfig.getRelevantSegmentIds();
      final Literal segmentIds = new Literal(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray());
      if (predicates.isPresent()) {
        query.where(new And(segmentIds, predicates.get()));
      } else {
        query.where(segmentIds);
      }
    } else {
      predicates.ifPresent(query::where);
    }

    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getFulltextRows(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRowsAND(List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions, String identifier, List<String> projection, ReadableQueryConfig qc) {
    /* Prepare plain query. */
    final Query query = new Query(this.fqn);
    if (projection.isEmpty()) {
      query.select("*");
    } else {
      query.select(projection.toArray(new String[]{}));
    }

    /* Process predicates. */
    final List<Predicate> atomics = conditions.stream().map(c -> {
      final Pair<String,Boolean> op = toOperator(c.getMiddle());
      return new Literal(c.getLeft(), op.first, c.getRight().stream().map(PrimitiveTypeProvider::toObject).toArray(), op.second);
    }).collect(Collectors.toList());
    final Optional<Predicate> predicates = atomics.stream().reduce(Or::new);
    if (qc != null && !qc.getRelevantSegmentIds().isEmpty()) {
      final Set<String> relevant = qc.getRelevantSegmentIds();
      final Literal segmentIds = new Literal(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray());
      if (predicates.isPresent()) {
        query.where(new And(segmentIds, predicates.get()));
      } else {
        query.where(segmentIds);
      }
    } else {
      predicates.ifPresent(query::where);
    }
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getRowsAND(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    final Query query = new Query(this.fqn).select(column);
    try {
      return toSingleCol(this.cottontail.client.query(query, null), column);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public List<PrimitiveTypeProvider> getUniqueValues(String column) {
    final Query query = new Query(this.fqn).distinct(column);
    try {
      return toSingleCol(this.cottontail.client.query(query, null), column);
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getUniqueValues(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  public Map<String, Integer> countDistinctValues(String column) {
    final Query query = new Query(this.fqn).select("*");
    final Map<String, Integer> count = new HashMap<>();
    try {
      final TupleIterator results = this.cottontail.client.query(query, null);
      while (results.hasNext()) {
        final TupleIterator.Tuple t = results.next();
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
    final Query query = new Query(this.fqn).select("*");
    try {
      return processResults(this.cottontail.client.query(query, null));
    } catch (StatusRuntimeException e) {
      LOGGER.warn("Error occurred during query execution in getAll(): {}", e.getMessage());
      return new ArrayList<>(0);
    }
  }

  @Override
  public boolean existsEntity(String name) {
    final AboutEntity about = new AboutEntity(this.cottontail.fqn(name));
    try {
      final TupleIterator results = this.cottontail.client.about(about, null);
      return results.hasNext();
    } catch (StatusRuntimeException e) {
      return false;
    }
  }

  @Override
  public boolean ping() {
    try {
      this.cottontail.client.ping();
      return true;
    } catch (StatusRuntimeException e) {
      return false;
    }
  }

  /**
   * Creates and returns a basic {@link Query} object for the given kNN parameters.
   *
   * @param k The k parameter used for kNN
   * @param vector The query vector (= float array).
   * @param column The name of the column that should be queried.
   * @param config The {@link ReadableQueryConfig} with additional parameters.
   * @return {@link Query}
   */
  private Query knn(int k, float[] vector, String column, ReadableQueryConfig config) {
    final Optional<float[]> weights = config.getDistanceWeights();
    final Set<String> relevant = config.getRelevantSegmentIds();
    final String distance = toDistance(config.getDistance().orElse(Distance.manhattan));
    final Query query = new Query(this.fqn);

    /* Add weights (optional). */
    if (weights.isPresent()) {
      query.knn(column, k, distance, vector, weights.get());
    } else {
      query.knn(column, k, distance, vector, null);
    }

    /* Add relevant segments (optional). */
    if (!relevant.isEmpty()) {
      query.where(new Literal(GENERIC_ID_COLUMN_QUALIFIER, "IN", relevant.toArray()));
    }

    return query;
  }

  /**
   * Converts a {@link TupleIterator} response generated by Cottontail DB into a {@link List} {@link Map}s that contain the results.
   *
   * @param results {@link TupleIterator} to gather the results from.
   * @return {@link List} of {@link Map}s that contains the results.
   */
  private static List<Map<String, PrimitiveTypeProvider>> processResults(TupleIterator results) {
    final List<Map<String, PrimitiveTypeProvider>> _return = new LinkedList<>();
    final StopWatch watch = StopWatch.createStarted();
    final Collection<String> columns = results.getColumns();
    while (results.hasNext()) {
      final TupleIterator.Tuple t = results.next();
      final Map<String,PrimitiveTypeProvider> map = new HashMap<>(results.getNumberOfColumns());
      for (String c : columns) {
        map.put(c, PrimitiveTypeProvider.fromObject(t.get(c)));
      }
      _return.add(map);
    }
    LOGGER.trace("Processed {} results in {} ms", _return.size(), watch.getTime(TimeUnit.MILLISECONDS));
    return _return;
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
      final TupleIterator.Tuple t = results.next();
      _return.add(PrimitiveTypeProvider.fromObject(t.get(colName)));
    }
    return _return;
  }

  /**
   * Converts a {@link TupleIterator} response generated by Cottontail DB into a {@link List} of {@link DistanceElement}s.
   *
   * @param response {@link TupleIterator} to gather the results from.
   * @param distanceElementClass The type of {@link DistanceElement} to create.
   * @return {@link List} of {@link DistanceElement}s.
   */
  private static <T extends DistanceElement> List<T> handleNearestNeighbourResponse(TupleIterator response, Class<? extends T> distanceElementClass) {
    final List<T> result = new LinkedList<>();
    while (response.hasNext()) {
      try {
        final TupleIterator.Tuple t = response.next();
        final String id = t.get(GENERIC_ID_COLUMN_QUALIFIER).toString(); /* This should be fine. */
        double distance = t.asDouble(DB_DISTANCE_VALUE_QUALIFIER);
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
  private static String toDistance(Distance distance) {
    switch (distance) {
      case manhattan:
        return Knn.Distance.L1.toString();
      case euclidean:
        return Knn.Distance.L2.toString();
      case squaredeuclidean:
        return Knn.Distance.L2SQUARED.toString();
      case chisquared:
        return Knn.Distance.CHISQUARED.toString();
      case cosine:
        return Knn.Distance.COSINE.toString();
      default:
        LOGGER.error("distance '{}' not supported by cottontail", distance);
        throw new IllegalArgumentException("Distance '"+ distance.toString() + "' not supported by Cottontail DB.");
    }
  }

  /**
   * Converts a Cineast {@link RelationalOperator} into the corresponding Cottontail DB representation.
   *
   * @param op {@link RelationalOperator} to convert.
   * @return {@link String} Name of Cottontail DB distance.
   */
  private static Pair<String, Boolean> toOperator(RelationalOperator op) {
    switch (op) {
      case EQ:
        return new Pair<>("==", false);
      case NEQ:
        return new Pair<>("==", true);
      case GEQ:
        return new Pair<>(">=", false);
      case LEQ:
        return new Pair<>("<=", false);
      case GREATER:
        return new Pair<>(">", false);
      case LESS:
        return new Pair<>("<", false);
      case BETWEEN:
        return new Pair<>("BETWEEN", false);
      case LIKE:
        return new Pair<>("LIKE", false);
      case NLIKE:
        return new Pair<>("LIKE", true);
      case MATCH:
        return new Pair<>("MATCH", false);
      case ISNULL:
        return new Pair<>("IS NULL", false);
      case ISNOTNULL:
        return new Pair<>("IS NULL", true);
      case IN:
        return new Pair<>("IN", false);
      default:
        throw new IllegalArgumentException("Operator '"+ op.toString() + "' not supported by Cottontail DB.");
    }
  }
}
