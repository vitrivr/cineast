package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType.TEXT;
import static org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType.VECTOR;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.ColumnDefinition;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Index;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Index.IndexType;
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


  private CottontailWrapper cottontail = new CottontailWrapper();


  @Override
  public void init() {
    cottontail.createSchema("cineast");
  }

  @Override
  public boolean createMultiMediaObjectsEntity() {

    ArrayList<ColumnDefinition> columns = new ArrayList<>(4);
    ColumnDefinition.Builder builder = ColumnDefinition.newBuilder();

    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[0]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[1]).setType(Type.INTEGER).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[2]).setType(Type.STRING).build());
    columns.add(builder.clear().setName(MediaObjectDescriptor.FIELDNAMES[3]).setType(Type.STRING).build());

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entity(MediaObjectDescriptor.ENTITY))
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
        .setEntity(CottontailMessageBuilder.entity(MediaObjectMetadataDescriptor.ENTITY))
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
        .setEntity(CottontailMessageBuilder.entity(MediaSegmentMetadataDescriptor.ENTITY))
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
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[5]).setType(Type.DOUBLE).build());
    columns.add(builder.clear().setName(MediaSegmentDescriptor.FIELDNAMES[6]).setType(Type.DOUBLE).build());

    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(CottontailMessageBuilder.entity(MediaSegmentDescriptor.ENTITY))
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    return true;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique, int length,
      String... featureNames) {
    final AttributeDefinition[] attributes = Arrays.stream(featureNames)
        .map(s -> new AttributeDefinition(s, VECTOR, length))
        .toArray(AttributeDefinition[]::new);
    return this.createFeatureEntity(featurename, unique, attributes);
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique,
      AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    final HashMap<String, String> hints = new HashMap<>(1);

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
      if (attribute.getType() == VECTOR && attribute.getLength() > 0) {
        builder.setLength(attribute.getLength());
      }
      columns.add(builder.build());
      builder.clear();
    }
    Entity entity = CottontailMessageBuilder.entity(CottontailMessageBuilder.CINEAST_SCHEMA, entityName);
    CreateEntityMessage message = CreateEntityMessage.newBuilder()
        .setEntity(entity)
        .addAllColumns(columns).build();

    cottontail.createEntityBlocking(message);

    for (AttributeDefinition attribute : attributes) {
      if (attribute.getType() == TEXT) {
        Index index = Index.newBuilder().setEntity(entity).setName("index-lucene-" + entity.getSchema().getName() + "_" + entityName + "_" + attribute.getName()).setType(IndexType.LUCENE).build();
        /* Cottontail ignores index params as of july 19 */
        CreateIndexMessage idxMessage = CreateIndexMessage.newBuilder().setIndex(index).addColumns(attribute.getName()).build();
        cottontail.createIndexBlocking(idxMessage);
      }
    }

    return true;
  }

  @Override
  public boolean existsEntity(String entityName) {
    return false;
  }

  @Override
  public boolean dropEntity(String entityName) {
    final Entity entity = CottontailMessageBuilder.entity(CottontailMessageBuilder.CINEAST_SCHEMA, entityName);
    cottontail.dropEntityBlocking(entity);
    return true;
  }

  @Override
  public void close() {
    this.cottontail.close();
  }

  public static final Type mapAttributeType(org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType type) {
    switch (type) {

      case BOOLEAN:
        return Type.BOOLEAN;
      case DOUBLE:
        return Type.DOUBLE;
      case VECTOR:
        return Type.FLOAT_VEC;
      case FLOAT:
        return Type.FLOAT;
      /*case GEOGRAPHY:
        return Type.GEOGRAPHY;
      case GEOMETRY:
        return Type.GEOMETRY;*/
      case INT:
        return Type.INTEGER;
      case LONG:
        return Type.LONG;
      case STRING:
      case TEXT:
        return Type.STRING;
      default:
        throw new RuntimeException("type " + type + " has no matching analogue in CottontailDB");
    }
  }

}
