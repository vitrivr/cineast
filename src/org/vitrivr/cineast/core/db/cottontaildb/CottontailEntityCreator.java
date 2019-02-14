package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.ColumnDefinition;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Type;
import java.util.ArrayList;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;

public class CottontailEntityCreator implements EntityCreator {

  private static final Schema SCHEMA = CottontailMessageBuilder.schemaFromName("cineast");

  private CottontailWrapper cottontail = new CottontailWrapper();

  @Override
  public boolean createMultiMediaObjectsEntity() {

    ArrayList<ColumnDefinition> columns = new ArrayList<>(4);
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();

    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[0]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[1]).setType(Type.INTEGER).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[2]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[3]).setType(Type.STRING).build());

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entityFromName(SCHEMA, MediaObjectDescriptor.ENTITY))
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    return true;
  }

  @Override
  public boolean createMetadataEntity() {
    return false;
  }

  @Override
  public boolean createSegmentMetadataEntity() {
    return false;
  }

  @Override
  public boolean createSegmentEntity() {
    return false;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique, String... featureNames) {
    return false;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique,
      AttributeDefinition... attributes) {
    return false;
  }

  @Override
  public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
    return false;
  }

  @Override
  public boolean createEntity(String entityName, AttributeDefinition... attributes) {
    return false;
  }

  @Override
  public boolean existsEntity(String entityName) {
    return false;
  }

  @Override
  public boolean dropEntity(String entityName) {
    return false;
  }

  @Override
  public void close() {

  }
}
