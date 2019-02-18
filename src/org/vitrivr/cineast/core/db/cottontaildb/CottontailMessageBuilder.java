package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.AtomicLiteralBooleanPredicate;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.From;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Knn;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Query;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}
