package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.From;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Predicate.Where;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Projection;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CottontailMessageBuilder {

  private static final Schema.Builder schemaBuilder = Schema.newBuilder();
  private static final Entity.Builder entityBuilder = Entity.newBuilder();
  private static final From.Builder fromBuilder = From.newBuilder();
  private static final Projection.Builder projectionBuilder = Projection.newBuilder();
  private static final Where.Builder whereBuilder = Where.newBuilder();

  public static final Schema CINEAST_SCHEMA = schemaFromName("cineast");

  public static Schema schemaFromName(String schema){
    synchronized (schemaBuilder){
      return schemaBuilder.clear().setName(schema).build();
    }
  }

  public static Entity entityFromName(Schema schema, String name){
    synchronized (entityBuilder){
      return entityBuilder.clear().setSchema(schema).setName(name).build();
    }
  }

  public static From fromFromEntity(Entity entity){
    synchronized (fromBuilder){
      return fromBuilder.clear().setEntity(entity).build();
    }
  }

  public static Projection projection(Projection.Operation operation, String... attributes){
    List<String> attrs = attributes == null ? Collections.emptyList() : Arrays.asList(attributes);
    synchronized (projectionBuilder){
     return projectionBuilder.clear().setOp(operation).addAllAttributes(attrs).build();
    }
  }

  public static Where where(String attribute, Where.Operation operation, Data... data){
    List<Data> datas = data == null ? Collections.emptyList() : Arrays.asList(data);
    synchronized (whereBuilder){
      return whereBuilder.clear().setAttribute(attribute).setOp(operation).addAllData(datas).build();
    }
  }

}
