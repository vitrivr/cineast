package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.AtomicLiteralBooleanPredicate;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DoubleVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.FloatVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.From;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.IntVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Knn;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.LongVector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Query;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Vector;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

public class CottontailMessageBuilder {

  private static final Schema.Builder schemaBuilder = Schema.newBuilder();
  public static final Schema CINEAST_SCHEMA = schema("cineast");
  private static final Entity.Builder entityBuilder = Entity.newBuilder();
  private static final From.Builder fromBuilder = From.newBuilder();
  private static final Projection.Builder projectionBuilder = Projection.newBuilder();
  private static final Where.Builder whereBuilder = Where.newBuilder();
  private static final AtomicLiteralBooleanPredicate.Builder atomicPredicateBuilder =
      AtomicLiteralBooleanPredicate.newBuilder();
  private static final Query.Builder queryBuilder = Query.newBuilder();
  private static final QueryMessage.Builder queryMessageBuilder = QueryMessage.newBuilder();

  private static final Vector.Builder vectorBuilder = Vector.newBuilder();
  private static final FloatVector.Builder floatVectorBuilder = FloatVector.newBuilder();
  private static final DoubleVector.Builder doubleVectorBuilder = DoubleVector.newBuilder();
  private static final IntVector.Builder intVectorBuilder = IntVector.newBuilder();
  private static final LongVector.Builder longVectorBuilder = LongVector.newBuilder();
  private static final Data.Builder dataBuilder = Data.newBuilder();



  public static Schema schema(String schema) {
    synchronized (schemaBuilder) {
      return schemaBuilder.clear().setName(schema).build();
    }
  }

  public static Entity entity(Schema schema, String name) {
    synchronized (entityBuilder) {
      return entityBuilder.clear().setSchema(schema).setName(name).build();
    }
  }

  public static Entity entity(String name){
    return entity(CINEAST_SCHEMA, name);
  }

  public static From from(Entity entity) {
    synchronized (fromBuilder) {
      return fromBuilder.clear().setEntity(entity).build();
    }
  }

  public static Projection projection(Projection.Operation operation, String... attributes) {
    List<String> attrs = attributes == null ? Collections.emptyList() : Arrays.asList(attributes);
    synchronized (projectionBuilder) {
      return projectionBuilder.clear().setOp(operation).addAllAttributes(attrs).build();
    }
  }

  public static AtomicLiteralBooleanPredicate atomicPredicate(
      String attribute,
      AtomicLiteralBooleanPredicate.Operator operator,
      boolean negation,
      Data... data) {
    synchronized (atomicPredicateBuilder) {
      atomicPredicateBuilder.clear().setAttribute(attribute).setNot(negation).setOp(operator);
      if (data != null) {
        for (Data d : data) {
          atomicPredicateBuilder.addData(d);
        }
      }
      return atomicPredicateBuilder.build();
    }
  }

  public static Where atomicWhere(
      String attribute,
      AtomicLiteralBooleanPredicate.Operator operator,
      boolean negation,
      Data... data) {

    synchronized (whereBuilder) {
      return whereBuilder
          .clear()
          .setAtomic(atomicPredicate(attribute, operator, negation, data))
          .build();
    }
  }


  public static Query query(Entity entity, Projection projection, Where where, Knn knn){
    synchronized (queryBuilder){
      queryBuilder.clear()
          .setFrom(from(entity))
          .setProjection(projection);
      if (where != null){
        queryBuilder.setWhere(where);
      }
      if (knn != null){
        queryBuilder.setKnn(knn);
      }
      return queryBuilder.build();
    }
  }

  public static Data toData(Object o) {

    synchronized (dataBuilder) {
      dataBuilder.clear();

      if (o == null) {
        return dataBuilder.setStringData("null").build();
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

      if (o instanceof String) {
        return dataBuilder.setStringData((String) o).build();
      }

      if (o instanceof float[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (floatVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setFloatVector(
                        floatVectorBuilder.addAllVector(Floats.asList((float[]) o))))
                .build();
          }
        }
      }

      if (o instanceof double[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (doubleVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setDoubleVector(
                        doubleVectorBuilder.addAllVector(Doubles.asList((double[]) o))))
                .build();
          }
        }
      }

      if (o instanceof int[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (intVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setIntVector(
                        intVectorBuilder.addAllVector(Ints.asList((int[]) o))))
                .build();
          }
        }
      }

      if (o instanceof long[]) {
        synchronized (vectorBuilder) {
          vectorBuilder.clear();
          synchronized (longVectorBuilder) {
            return dataBuilder
                .setVectorData(
                    vectorBuilder.setLongVector(
                        longVectorBuilder.addAllVector(Longs.asList((long[]) o))))
                .build();
          }
        }
      }

      return dataBuilder.setStringData(o.toString()).build();
    }
  }

  public static PrimitiveTypeProvider fromData(Data d){

    switch(d.getDataCase()){

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
      case VECTORDATA:{

        Vector v = d.getVectorData();
        switch(v.getVectorDataCase()){

          case FLOATVECTOR:
            return FloatArrayTypeProvider.fromList(v.getFloatVector().getVectorList());
          case DOUBLEVECTOR:
            return FloatArrayTypeProvider.fromDoubleList(v.getDoubleVector().getVectorList());
          case INTVECTOR:
            return IntArrayTypeProvider.fromList(v.getIntVector().getVectorList());
          case LONGVECTOR:
            return IntArrayTypeProvider.fromLongList(v.getLongVector().getVectorList());
          case VECTORDATA_NOT_SET:
            return new NothingProvider();
        }

      }

      case DATA_NOT_SET:
        return new NothingProvider();
    }

    return new NothingProvider();

  }

  public static List<Data> toData(Iterable<Object> obs){

    List<Data> _return = new ArrayList<>();

    for(Object o: obs){
      _return.add(toData(o));
    }

    return _return;

  }

  public static QueryMessage queryMessage(Query query, String queryId){
    synchronized (queryMessageBuilder){
      return queryMessageBuilder.clear().setQuery(query).setQueryId(queryId).build();
    }
  }

}
