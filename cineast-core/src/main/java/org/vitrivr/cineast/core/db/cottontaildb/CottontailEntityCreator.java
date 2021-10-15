package org.vitrivr.cineast.core.db.cottontaildb;


import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.BITSET;
import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.TEXT;
import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.VECTOR;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import io.grpc.StatusRuntimeException;
import java.util.Objects;
import java.util.Optional;
import org.vitrivr.cottontail.client.TupleIterator;
import org.vitrivr.cottontail.client.language.basics.Constants;
import org.vitrivr.cottontail.client.language.basics.Type;
import org.vitrivr.cottontail.client.language.ddl.AboutEntity;
import org.vitrivr.cottontail.client.language.ddl.CreateEntity;
import org.vitrivr.cottontail.client.language.ddl.CreateIndex;
import org.vitrivr.cottontail.client.language.ddl.CreateSchema;
import org.vitrivr.cottontail.client.language.ddl.DropEntity;

import java.util.Arrays;
import java.util.HashMap;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cottontail.client.language.ddl.ListSchemas;
import org.vitrivr.cottontail.grpc.CottontailGrpc.IndexType;

public final class CottontailEntityCreator implements EntityCreator {

  public static final String COTTONTAIL_PREFIX = "cottontail";
  public static final String INDEX_HINT = COTTONTAIL_PREFIX+".index";

  /** Internal reference to the {@link CottontailWrapper} used by this {@link CottontailEntityCreator}. */
  private final CottontailWrapper cottontail;

  public CottontailEntityCreator(DatabaseConfig config) {
    this.cottontail = new CottontailWrapper(config, false);
    init();
  }

  public CottontailEntityCreator(CottontailWrapper cottontailWrapper) {
    this.cottontail = cottontailWrapper;
    init();
  }

  /**
   * Makes sure that schema 'cineast' is available.
   */
  private void init() {
    final long txId = this.cottontail.client.begin();
    try {
      final ListSchemas list = new ListSchemas();
      final TupleIterator iterator = this.cottontail.client.list(list, txId);
      boolean exists = false;
      while (iterator.hasNext()) {
        TupleIterator.Tuple next = iterator.next();
        if (Objects.equals(next.asString(Constants.COLUMN_NAME_DBO), CottontailWrapper.FQN_CINEAST_SCHEMA)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        this.cottontail.client.create(new CreateSchema(CottontailWrapper.CINEAST_SCHEMA), txId);
      }
      this.cottontail.client.commit(txId);
    } catch (StatusRuntimeException e) {
      LOGGER.error("Error during initialization", e);
      this.cottontail.client.rollback(txId);
    }
  }

  @Override
  public boolean createTagEntity() {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + TagReader.TAG_ENTITY_NAME;
      final CreateEntity create = new CreateEntity(entityName)
          .column(TagReader.TAG_ID_COLUMNNAME, Type.STRING, -1, false)
          .column(TagReader.TAG_NAME_COLUMNNAME, Type.STRING, -1, false)
          .column(TagReader.TAG_DESCRIPTION_COLUMNNAME, Type.STRING, -1, false);
      this.cottontail.client.create(create, txId);

      /* tag ids should be unique */
      this.createIndex(entityName, TagReader.TAG_ID_COLUMNNAME, IndexType.HASH_UQ, txId);
      /* tag names do not necessarily have to be unique */
      this.createIndex(entityName, TagReader.TAG_NAME_COLUMNNAME, IndexType.HASH, txId);
      /* could be used for autocomplete */
      this.createIndex(entityName, TagReader.TAG_NAME_COLUMNNAME, IndexType.LUCENE, txId);

      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createMultiMediaObjectsEntity() {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + MediaObjectDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(entityName)
          .column(MediaObjectDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[1], Type.INTEGER, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[3], Type.STRING, -1, false);
      this.cottontail.client.create(entity, txId);

      /* Create index. */
      this.createIndex(entityName, MediaObjectDescriptor.FIELDNAMES[0], IndexType.HASH_UQ, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createSegmentEntity() {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + MediaSegmentDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(entityName)
          .column(MediaSegmentDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[2], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[3], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[4], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[5], Type.DOUBLE, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[6], Type.DOUBLE, -1, false);
      this.cottontail.client.create(entity, txId);

      /* Create indexes. */
      this.createIndex(entityName, MediaSegmentDescriptor.FIELDNAMES[0], IndexType.HASH_UQ, txId);
      this.createIndex(entityName, MediaSegmentDescriptor.FIELDNAMES[1], IndexType.HASH, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createMetadataEntity() {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + MediaObjectMetadataDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(entityName)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[3], Type.STRING, -1, false);
      this.cottontail.client.create(entity, txId);

      /* Create Index. */
      this.createIndex(entityName, MediaObjectMetadataDescriptor.FIELDNAMES[0], IndexType.HASH, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createSegmentMetadataEntity() {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + MediaSegmentMetadataDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(entityName)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[3], Type.STRING, -1, false);
      this.cottontail.client.create(entity, txId);

      /* Create Index. */
      this.createIndex(entityName, MediaSegmentMetadataDescriptor.FIELDNAMES[0], IndexType.HASH, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }


  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique, int length, String... featureNames) {
    final AttributeDefinition[] attributes = Arrays.stream(featureNames)
        .map(s -> new AttributeDefinition(s, VECTOR, length))
        .toArray(AttributeDefinition[]::new);
    return this.createFeatureEntity(featureEntityName, unique, attributes);
  }

  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique, AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    final HashMap<String, String> hints = new HashMap<>(1);
    extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING, hints);
    System.arraycopy(attributes, 0, extended, 1, attributes.length);
    return this.createEntity(featureEntityName, extended);
  }

  @Override
  public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING);
    System.arraycopy(attributes, 0, extended, 1, attributes.length);
    return this.createEntity(entityName, extended);
  }

  @Override
  public boolean createEntity(String entityName, AttributeDefinition... attributes) {
    return this.createEntity(
        new org.vitrivr.cineast.core.db.setup.EntityDefinition.EntityDefinitionBuilder(entityName).withAttributes(attributes).build()
    );
  }

  @Override
  public boolean createEntity(org.vitrivr.cineast.core.db.setup.EntityDefinition def) {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String entityName = CottontailWrapper.CINEAST_SCHEMA + "." + def.getEntityName();
      final CreateEntity entity = new CreateEntity(entityName);
      for (AttributeDefinition attribute : def.getAttributes()) {
        int length = -1;
        if ((attribute.getType() == VECTOR || attribute.getType() == BITSET) && attribute.getLength() > 0) {
          length = attribute.getLength();
        }
        entity.column(attribute.getName(), mapAttributeType(attribute.getType()), length, false);

      }
      this.cottontail.client.create(entity, txId);

      /* Create Index. */
      for (AttributeDefinition attribute : def.getAttributes()) {
        if (attribute.getType() == TEXT) {
          this.createIndex(CottontailWrapper.CINEAST_SCHEMA + "." + def.getEntityName(), attribute.getName(), IndexType.LUCENE, txId);
        }
        // TODO (LS, 18.11.2020) Shouldn't we also have abstract indices in the db abstraction layer?
        final Optional<String> hint = attribute.getHint(INDEX_HINT);
        if (hint.isPresent()){
          IndexType idx = IndexType.valueOf(hint.get());
          this.createIndex(CottontailWrapper.CINEAST_SCHEMA + "." + def.getEntityName(), attribute.getName(), idx, txId);
        }
      }
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createHashNonUniqueIndex(String entityName, String column) {
    final long txId = this.cottontail.client.begin();
    try {
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + entityName;
      this.createIndex(fqn, column, IndexType.HASH, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean existsEntity(String entityName) {
    final AboutEntity about = new AboutEntity(this.cottontail.fqnInput(entityName));
    try {
      final TupleIterator results = this.cottontail.client.about(about, null);
      return results.hasNext();
    } catch (StatusRuntimeException e) {
      return false;
    }
  }

  @Override
  public boolean dropEntity(String entityName) {
    final long txId = this.cottontail.client.begin();
    try {
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + entityName;
      this.cottontail.client.drop(new DropEntity(fqn), txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public void close() {
    this.cottontail.close();
  }

  public static Type mapAttributeType(AttributeDefinition.AttributeType type) {
    switch (type) {
      case BOOLEAN:
        return Type.BOOLEAN;
      case DOUBLE:
        return Type.DOUBLE;
      case VECTOR:
        return Type.FLOAT_VECTOR;
      case BITSET:
        return Type.BOOL_VECTOR;
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
        throw new RuntimeException("type " + type + " has no matching analogue in Cottontail DB");
    }
  }


  private void createIndex(String entityName, String attribute, IndexType type, long txId) {
    final String indexName = entityName + ".idx_" + attribute + "_" + type.name().toLowerCase();
    final CreateIndex index = new CreateIndex(indexName, type).column(entityName + "." + attribute);
    this.cottontail.client.create(index, txId);
  }
}
