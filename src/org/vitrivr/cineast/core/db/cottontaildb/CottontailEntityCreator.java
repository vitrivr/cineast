package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.ColumnDefinition;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
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

    ArrayList<ColumnDefinition> columns = new ArrayList<>(4);
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();

    columns.add(builder.clear().setName(MediaObjectMetadataDescriptor.FIELDNAMES[0]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectMetadataDescriptor.FIELDNAMES[1]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectMetadataDescriptor.FIELDNAMES[2]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectMetadataDescriptor.FIELDNAMES[3]).setType(Type.STRING).build());

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entityFromName(SCHEMA, MediaObjectMetadataDescriptor.ENTITY))
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    return true;
  }

  @Override
  public boolean createSegmentMetadataEntity() {
    ArrayList<ColumnDefinition> columns = new ArrayList<>(4);
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();

    columns.add(builder.clear().setName(MediaSegmentMetadataDescriptor.FIELDNAMES[0]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaSegmentMetadataDescriptor.FIELDNAMES[1]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaSegmentMetadataDescriptor.FIELDNAMES[2]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaSegmentMetadataDescriptor.FIELDNAMES[3]).setType(Type.STRING).build());

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entityFromName(SCHEMA, MediaSegmentMetadataDescriptor.ENTITY))
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    return true;
  }

  @Override
  public boolean createSegmentEntity() {
    ArrayList<ColumnDefinition> columns = new ArrayList<>(4);
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();

    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[0]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[1]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[2]).setType(Type.INTEGER).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[3]).setType(Type.INTEGER).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[4]).setType(Type.INTEGER).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[5]).setType(Type.INTEGER).build()); //FIXME should be double
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[6]).setType(Type.INTEGER).build()); //FIXME should be double

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entityFromName(SCHEMA, MediaSegmentDescriptor.ENTITY))
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    return true;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique, String... featureNames) {
    final AttributeDefinition[] attributes = Arrays.stream(featureNames)
        .map(s -> new AttributeDefinition(s, AttributeDefinition.AttributeType.VECTOR))
        .toArray(AttributeDefinition[]::new);
    return this.createFeatureEntity(featurename, unique, attributes);
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique,
      AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    final HashMap<String,String> hints = new HashMap<>(1);

    extended[0] = new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING, hints);
    System.arraycopy(attributes, 0, extended, 1, attributes.length);
    return this.createEntity(featurename, extended);
  }

  @Override
  public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    extended[0] = new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING);
    System.arraycopy(attributes, 0, extended, 1, attributes.length);
    return this.createEntity(entityName, extended);
  }

  @Override
  public boolean createEntity(String entityName, AttributeDefinition... attributes) {

    ArrayList<ColumnDefinition> columns = new ArrayList<>();
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();
    for (AttributeDefinition attribute : attributes) {
      builder.setName(attribute.getName()).setType(mapAttributeType(attribute.getType()));
      columns.add(builder.build());
      builder.clear();
    }

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entityFromName(SCHEMA,entityName))
        .addAllColumns(columns).build();

    return true;
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
    this.cottontail.close();
  }

  public static final Type mapAttributeType(org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType type) {
    switch (type) {

      case BOOLEAN:
        return Type.BOOLEAN;
     // case DOUBLE:
     //   return Type.DOUBLE;
      case VECTOR:
        return Type.DOUBLE_ARRAY;
     // case FLOAT:
     //   return Type.FLOAT;
      /*case GEOGRAPHY:
        return Type.GEOGRAPHY;
      case GEOMETRY:
        return Type.GEOMETRY;*/
      case INT:
        return Type.INTEGER;
      case LONG:
        return Type.LONG;
      case STRING:
        return Type.STRING;
      case TEXT:
     //   return Type.TEXT;
      default:
        throw new RuntimeException("type " + type + " has no matching analogue in CottontailDB");
    }
  }

}
