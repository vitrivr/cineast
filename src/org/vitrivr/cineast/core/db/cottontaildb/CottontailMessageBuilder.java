package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;

public class CottontailMessageBuilder {

  private static final Schema.Builder schemaBuilder = Schema.newBuilder();
  private static final Entity.Builder entityBuilder = Entity.newBuilder();

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

}
