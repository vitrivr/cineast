package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.RelationalOperator.NEQ;
import static org.vitrivr.cineast.core.db.RelationalOperator.NLIKE;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.googlecode.javaewah.datastructure.BitSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.providers.primitive.BitSetTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.BooleanTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.DoubleTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.IntArrayTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.IntTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.LongTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.AtomicLiteralBooleanPredicate;
import org.vitrivr.cottontail.grpc.CottontailGrpc.BoolVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ColumnName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ComparisonOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.CompoundBooleanPredicate;
import org.vitrivr.cottontail.grpc.CottontailGrpc.ConnectionOperator;
import org.vitrivr.cottontail.grpc.CottontailGrpc.DoubleVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.FloatVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.From;
import org.vitrivr.cottontail.grpc.CottontailGrpc.IntVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Knn;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Knn.Distance;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Literal;
import org.vitrivr.cottontail.grpc.CottontailGrpc.LongVector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Projection.ProjectionElement;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Query;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage.Tuple;
import org.vitrivr.cottontail.grpc.CottontailGrpc.SchemaName;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Vector;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Where;

public class CottontailMessageBuilder {

  private static final Logger LOGGER = LogManager.getLogger();

  public static final SchemaName CINEAST_SCHEMA =  SchemaName.newBuilder().setName("cineast").build();

  /**
   * Returns the [EntityName] object for the given entity name.
   *
   * @param name Name of the entity.
   *
   * @return [EntityName]
   */
  public static EntityName entity(String name) {
    return EntityName.newBuilder().setSchema(CINEAST_SCHEMA).setName(name).build();
  }

  /**
   * Returns the {@link ColumnName} object for the given entity name.
   *
   * @param name Name of the entity.
   *
   * @return {@link ColumnName}
   */
  public static ColumnName column(String name) {
    return ColumnName.newBuilder().setName(name).build();
  }

  /**
   * Generates and returns the {@link From} object for the given entity name.
   *
   * @param from {@link EntityName} of the entity to select from.
   * @return {@link From}
   */
  public static From from(EntityName from) {
    return From.newBuilder().setEntity(from).build();
  }

  /**
   * Generates and returns a {@link Projection} for the given {@link Projection.ProjectionOperation} and list of attributes.
   *
   * @param operation {@link Projection.ProjectionOperation}
   * @param attributes List of attributes (column names).
   * @return {@link Projection}
   */
  public static Projection projection(Projection.ProjectionOperation operation, String... attributes) {
    List<String> attrs = attributes == null ? Collections.emptyList() : Arrays.asList(attributes);
    final Projection.Builder projection = Projection.newBuilder().setOp(operation);
    attrs.forEach(e -> projection.addColumns(ProjectionElement.newBuilder().setColumn(column(e))));
    return projection.build();
  }

  /**
   * Converts a {@link RelationalOperator} into the {@link ComparisonOperator} equivalent.
   *
   * @param op {@link RelationalOperator} to convert
   * @return {@link ComparisonOperator}
   */
  public static ComparisonOperator toOperator(RelationalOperator op) {
    switch (op) {
      case EQ:
      case NEQ: //this has to be not-ed!!
        return ComparisonOperator.EQUAL;
      case GEQ:
        return ComparisonOperator.GEQUAL;
      case LEQ:
        return ComparisonOperator.LEQUAL;
      case GREATER:
        return ComparisonOperator.GREATER;
      case LESS:
        return ComparisonOperator.LESS;
      case BETWEEN:
        return ComparisonOperator.BETWEEN;
      case LIKE:
      case NLIKE: //this has to be not-ed!!
        return ComparisonOperator.LIKE;
      case MATCH: // 1:1 relationship between Cottontail DB MATCH and MATCHES
        return ComparisonOperator.MATCH;
      case ISNULL:
        return ComparisonOperator.ISNULL;
      case ISNOTNULL:
        return ComparisonOperator.ISNOTNULL;
      case IN:
        return ComparisonOperator.IN;
      default:
        throw new UnsupportedOperationException(op.toString());
    }
  }

  /**
   * Generates a {@link AtomicLiteralBooleanPredicate} for the given parameters.
   *
   * @param attribute The name of the attribute / column to compare.
   * @param operator The {@link RelationalOperator} to compare with.
   * @param data The {@link Literal}(s) to compare to.
   * @return {@link AtomicLiteralBooleanPredicate}
   */
  public static AtomicLiteralBooleanPredicate atomicPredicate(String attribute, RelationalOperator operator, Literal... data) {
    AtomicLiteralBooleanPredicate.Builder builder = AtomicLiteralBooleanPredicate.newBuilder().setLeft(column(attribute));
    if (data != null) {
      for (Literal d : data) {
        builder.addRight(d);
      }
    }
    builder.setOp(toOperator(operator));
    if (operator == NEQ || operator == NLIKE) {
      builder.setNot(true);
    }
    return builder.build();
  }

  /**
   * Generates a {@link Where} for the given parameters.
   *
   * @param attribute The name of the attribute / column to compare.
   * @param operator The {@link RelationalOperator} to compare with.
   * @param data The {@link Literal}(s) to compare to.
   * @return {@link AtomicLiteralBooleanPredicate}
   */
  public static Where atomicWhere(String attribute, RelationalOperator operator, Literal... data) {
    return Where.newBuilder().setAtomic(atomicPredicate(attribute, operator, data)).build();
  }

  /**
   * Generates a {@link Where} for the given parameters.
   *
   * @param attribute The name of the attribute / column to compare.
   * @param operator The {@link RelationalOperator} to compare with.
   * @param data The {@link Literal}(s) to compare to.
   * @return {@link AtomicLiteralBooleanPredicate}
   */
  public static List<AtomicLiteralBooleanPredicate> toAtomicLiteralBooleanPredicates(String attribute, RelationalOperator operator, Literal... data) {
    if (data == null || data.length == 0) {
      return Collections.emptyList();
    }
    ArrayList<AtomicLiteralBooleanPredicate> _return = new ArrayList<>(data.length);
    for (Literal d : data) {
      _return.add(
          atomicPredicate(attribute, operator, d)
      );
    }
    return _return;
  }

  /**
   * Generates and returns a {@link CompoundBooleanPredicate} for the given parameters.
   *
   * @param op {@link ConnectionOperator} to use.
   * @param predicates List of {@link AtomicLiteralBooleanPredicate} to connect.
   * @return {@link CompoundBooleanPredicate}
   */
  private static CompoundBooleanPredicate reduce(ConnectionOperator op, List<AtomicLiteralBooleanPredicate> predicates) {
    if (predicates == null || predicates.size() < 2) {
      throw new IllegalArgumentException("CottontailMessageBuilder.reduce needs at least 2 predicates");
    }

    CompoundBooleanPredicate _return = CompoundBooleanPredicate.newBuilder()
        .setAleft(predicates.get(predicates.size() - 2))
        .setOp(op)
        .setAright(predicates.get(predicates.size() - 1))
        .build();

    for (int i = predicates.size() - 3; i >= 0; --i) {
      _return = CompoundBooleanPredicate.newBuilder()
          .setAleft(predicates.get(i))
          .setOp(op)
          .setCright(_return)
          .build();
    }

    return _return;
  }

  public static Where compoundWhere(ReadableQueryConfig queryConfig, String fieldname, RelationalOperator operator, ConnectionOperator op, Literal... data) {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("data not set in CottontailMessageBuilder.compoundOrWhere");
    }

    AtomicLiteralBooleanPredicate inList = null;
    if (queryConfig != null && queryConfig.hasRelevantSegmentIds()) {
      final List<Literal> segments = toDatas(queryConfig.getRelevantSegmentIds());
      inList = atomicPredicate(GENERIC_ID_COLUMN_QUALIFIER, RelationalOperator.IN, segments.toArray(new Literal[0]));
    }

    List<AtomicLiteralBooleanPredicate> predicates = toAtomicLiteralBooleanPredicates(fieldname, operator, data);

    if (inList == null) {

      if (predicates.size() > 1) {
        return Where.newBuilder().setCompound(
            reduce(op, predicates)
        ).build();
      } else {
        return Where.newBuilder().setAtomic(predicates.get(0)).build();
      }

    } else {
      /* A match for the ids is mandatory, that's why there's an and here */
      CompoundBooleanPredicate.Builder builder = CompoundBooleanPredicate.newBuilder().setAleft(inList).setOp(ConnectionOperator.AND);
      if (predicates.size() > 1) {
        builder.setCright(reduce(op, predicates));
      } else {
        builder.setAright(predicates.get(0));
      }

      return Where.newBuilder().setCompound(builder).build();

    }
  }

  public static Where compoundWhere(ReadableQueryConfig qc, List<Triple<String, RelationalOperator, List<PrimitiveTypeProvider>>> conditions) {
    if (conditions.size() == 0) {
      throw new IllegalArgumentException("no condition given");
    }
    AtomicLiteralBooleanPredicate inList = null;
    if (qc != null && qc.hasRelevantSegmentIds()) {
      final List<Literal> segments = toDatas(qc.getRelevantSegmentIds());
      inList = atomicPredicate(GENERIC_ID_COLUMN_QUALIFIER, RelationalOperator.IN, segments.toArray(new Literal[0]));
    }

    List<AtomicLiteralBooleanPredicate> predicates = conditions.stream().map(cond -> atomicPredicate(cond.getLeft(), cond.getMiddle(), toData(cond.getRight()))).collect(Collectors.toList());
    if (inList != null) {
      predicates.add(0, inList);
    }
    if (predicates.size() > 1) {
      return Where.newBuilder().setCompound(reduce(ConnectionOperator.AND, predicates)).build();
    }
    return Where.newBuilder().setAtomic(predicates.get(0)).build();
  }

  public static Query query(EntityName entity, Projection projection, Where where, Knn knn, Integer rows) {
    Query.Builder queryBuilder = Query.newBuilder();
    queryBuilder.setFrom(from(entity)).setProjection(projection);
    if (where != null) {
      queryBuilder.setWhere(where);
    }
    if (knn != null) {
      queryBuilder.setKnn(knn);
    }
    if (rows != null) {
      queryBuilder.setLimit(rows);
    }
    return queryBuilder.build();
  }

  /**
   * Converts an {@link Object} to a {@link Literal}
   *
   * @param o {@link Object} to convert
   * @return {@link Literal}
   */
  public static Literal toData(Object o) {

    Literal.Builder dataBuilder = Literal.newBuilder();

    if (o == null) {
      return dataBuilder.setStringData("null").build();
    }

    if (o instanceof PrimitiveTypeProvider) {
      o = PrimitiveTypeProvider.getObject((PrimitiveTypeProvider) o);
    }

    if (o instanceof Boolean) {
      return dataBuilder.setBooleanData((boolean) o).build();
    }

    if (o instanceof Integer) {
      return dataBuilder.setIntData((int) o).build();
    }

    if (o instanceof Float) {
      return dataBuilder.setFloatData((float) o).build();
    }

    if (o instanceof Double) {
      return dataBuilder.setDoubleData((double) o).build();
    }

    if (o instanceof Long) {
      return dataBuilder.setLongData((long) o).build();
    }

    if (o instanceof String) {
      return dataBuilder.setStringData((String) o).build();
    }

    if (o instanceof float[]) {
      return dataBuilder.setVectorData(toVector((float[]) o)).build();
    }

    if (o instanceof BitSet) {
      final boolean[] vector = new boolean[((BitSet) o).size()];
      for (int i = 0; i < ((BitSet) o).size(); i++) {
        vector[i] = ((BitSet) o).get(i);
      }
      return dataBuilder.setVectorData(toVector(vector)).build();
    }

    if (o instanceof ReadableFloatVector) {
      return dataBuilder.setVectorData(toVector(((ReadableFloatVector) o))).build();
    }

    Vector.Builder vectorBuilder = Vector.newBuilder();

    if (o instanceof double[]) {
      vectorBuilder.clear();
      return dataBuilder
          .setVectorData(
              vectorBuilder.setDoubleVector(
                  DoubleVector.newBuilder().addAllVector(Doubles.asList((double[]) o))))
          .build();
    }

    if (o instanceof int[]) {

      vectorBuilder.clear();
      return dataBuilder
          .setVectorData(
              vectorBuilder.setIntVector(
                  IntVector.newBuilder().addAllVector(Ints.asList((int[]) o))))
          .build();
    }

    if (o instanceof long[]) {
      vectorBuilder.clear();
      return dataBuilder
          .setVectorData(
              vectorBuilder.setLongVector(
                  LongVector.newBuilder().addAllVector(Longs.asList((long[]) o))))
          .build();
    }

    LOGGER.debug("Unknown type {} in message builder, serializing to string representation {}", o.getClass().getName(), o.toString());

    return dataBuilder.setStringData(o.toString()).build();
  }

  /**
   * Converts a {@link Literal} to a {@link PrimitiveTypeProvider}.
   *
   * @param d {@link Literal} to convert
   * @return {@link PrimitiveTypeProvider}
   */
  public static PrimitiveTypeProvider fromData(Literal d) {
    switch (d.getDataCase()) {
      case BOOLEANDATA:
        return new BooleanTypeProvider(d.getBooleanData());
      case INTDATA:
        return new IntTypeProvider(d.getIntData());
      case LONGDATA:
        return new LongTypeProvider(d.getLongData());
      case FLOATDATA:
        return new FloatTypeProvider(d.getFloatData());
      case DOUBLEDATA:
        return new DoubleTypeProvider(d.getDoubleData());
      case STRINGDATA:
        return new StringTypeProvider(d.getStringData());
      case VECTORDATA: {
        Vector v = d.getVectorData();
        switch (v.getVectorDataCase()) {
          case FLOATVECTOR:
            return FloatArrayTypeProvider.fromList(v.getFloatVector().getVectorList());
          case DOUBLEVECTOR:
            return FloatArrayTypeProvider.fromDoubleList(v.getDoubleVector().getVectorList());
          case INTVECTOR:
            return IntArrayTypeProvider.fromList(v.getIntVector().getVectorList());
          case LONGVECTOR:
            return IntArrayTypeProvider.fromLongList(v.getLongVector().getVectorList());
          case BOOLVECTOR:
            return BitSetTypeProvider.fromBooleanList(v.getBoolVector().getVectorList());
          case VECTORDATA_NOT_SET:
            return new NothingProvider();
        }
      }
      case DATA_NOT_SET:
        return new NothingProvider();
    }

    return new NothingProvider();
  }

  /**
   * Converts a array of floats to a {@link Vector}.
   *
   * @param vector Float array to convert.
   * @return {@link Vector}
   */
  private static Vector toVector(float[] vector) {
    return Vector.newBuilder()
        .setFloatVector(FloatVector.newBuilder().addAllVector(Floats.asList(vector)))
        .build();
  }

  /**
   * Converts a array of booleans to a {@link Vector}.
   *
   * @param vector Boolean array to convert.
   * @return {@link Vector}
   */
  private static Vector toVector(boolean[] vector) {
    return Vector.newBuilder()
        .setBoolVector(BoolVector.newBuilder().addAllVector(Booleans.asList(vector)))
        .build();
  }

  /**
   * Converts a array of floats to a {@link Vector}.
   *
   * @param vector Float array to convert.
   * @return {@link Vector}
   */
  private static Vector toVector(ReadableFloatVector vector) {
    List<Float> floats = new ArrayList<>();
    for (int i = 0; i < vector.getElementCount(); i++) {
      floats.add(vector.getElement(i));
    }
    return Vector.newBuilder().setFloatVector(FloatVector.newBuilder().addAllVector(floats)).build();
  }

  public static List<Literal> toDatas(Iterable<?> obs) {
    final List<Literal> _return = new LinkedList<>();
    for (Object o : obs) {
      _return.add(toData(o));
    }
    return _return;
  }

  public static List<Literal> toDatas(Collection<?> obs) {
    final List<Literal> _return = new ArrayList<>(obs.size());
    for (Object o : obs) {
      _return.add(toData(o));
    }
    return _return;
  }

  /**
   * Generates and returns a {@link QueryMessage} for the given {@link Query}.
   *
   * @param query {@link Query}
   * @return {@link QueryMessage}
   */
  public static QueryMessage queryMessage(Query query) {
    return QueryMessage.newBuilder().setQuery(query).build();
  }

  /**
   * Converts a {@link Tuple} to a list of cs
   *
   * @param tuple {@link Tuple} to convert.
   * @return List of {@link PrimitiveTypeProvider}s
   */
  public static List<PrimitiveTypeProvider> query(Tuple tuple) {
    if (tuple == null) {
      return null;
    }
    return tuple.getDataList().stream().map(CottontailMessageBuilder::fromData).collect(Collectors.toList());
  }

  /**
   * Generates a {@link Knn} predicate for the given parameters.
   *
   * @param attribute Name of the attribute / column to query.
   * @param vector The query vector.
   * @param weights The weights vector.
   * @param k The k in kNN
   * @param distance The {@link ReadableQueryConfig.Distance} to use.
   * @return {@link Knn}
   */
  public static Knn knn(
      String attribute,
      float[] vector,
      float[] weights,
      int k,
      ReadableQueryConfig.Distance distance) {
    Knn.Builder knnBuilder = Knn.newBuilder();
    knnBuilder.clear().setK(k).addQuery(toVector(vector)).setAttribute(column(attribute));

    if (weights != null) {
      knnBuilder.addWeights(toVector(weights));
    }

    switch (distance) {
      case manhattan: {
        knnBuilder.setDistance(Distance.L1);
        break;
      }
      case euclidean: {
        knnBuilder.setDistance(Distance.L2);
        break;
      }
      case squaredeuclidean: {
        knnBuilder.setDistance(Distance.L2SQUARED);
        break;
      }
      case chisquared: {
        knnBuilder.setDistance(Distance.CHISQUARED);
        break;
      }
      default: {
        LOGGER.error("distance '{}' not supported by cottontail", distance);
        break;
      }
    }
    return knnBuilder.build();
  }

  /**
   * Generates a batched{@link Knn} predicate for the given parameters.
   *
   * @param attribute Name of the attribute / column to query.
   * @param vectors List of query vectors.
   * @param weights List of weight vectors.
   * @param k The k in kNN
   * @param distance The {@link ReadableQueryConfig.Distance} to use.
   * @return {@link Knn}
   */
  public static Knn batchedKnn(
      String attribute,
      List<float[]> vectors,
      List<float[]> weights,
      int k,
      ReadableQueryConfig.Distance distance
  ) {
    Knn.Builder knnBuilder = Knn.newBuilder();
    knnBuilder.clear().setK(k).setAttribute(column(attribute));
    vectors.forEach(v -> knnBuilder.addQuery(toVector(v)));

    if (weights != null) {
      weights.forEach(w -> knnBuilder.addWeights(toVector(w)));
    }

    switch (distance) {
      case manhattan: {
        knnBuilder.setDistance(Distance.L1);
        break;
      }
      case euclidean: {
        knnBuilder.setDistance(Distance.L2);
        break;
      }
      case squaredeuclidean: {
        knnBuilder.setDistance(Distance.L2SQUARED);
        break;
      }
      case chisquared: {
        knnBuilder.setDistance(Distance.CHISQUARED);
        break;
      }
      default: {
        LOGGER.error("distance '{}' not supported by cottontail", distance);
        break;
      }
    }

    return knnBuilder.build();
  }

  /**
   * Builds a where clause from a collection of strings. Returns null for an empty collection.
   */
  public static Where whereInList(String attribute, Collection<String> elements) {
    if (elements == null || elements.isEmpty()) {
      return null;
    }
    Where.Builder builder = Where.newBuilder();
    builder.setAtomic(atomicPredicate(attribute, RelationalOperator.IN, elements.toArray(new Literal[0])));
    return builder.build();
  }
}
