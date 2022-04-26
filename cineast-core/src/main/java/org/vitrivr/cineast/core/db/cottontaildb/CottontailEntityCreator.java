package org.vitrivr.cineast.core.db.cottontaildb;


import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.BITSET;
import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.TEXT;
import static org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType.VECTOR;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition.EntityDefinitionBuilder;
import org.vitrivr.cottontail.client.iterators.Tuple;
import org.vitrivr.cottontail.client.iterators.TupleIterator;
import org.vitrivr.cottontail.client.language.basics.Constants;
import org.vitrivr.cottontail.client.language.basics.Type;
import org.vitrivr.cottontail.client.language.ddl.AboutEntity;
import org.vitrivr.cottontail.client.language.ddl.CreateEntity;
import org.vitrivr.cottontail.client.language.ddl.CreateIndex;
import org.vitrivr.cottontail.client.language.ddl.CreateSchema;
import org.vitrivr.cottontail.client.language.ddl.DropEntity;
import org.vitrivr.cottontail.client.language.ddl.ListSchemas;
import org.vitrivr.cottontail.grpc.CottontailGrpc.IndexType;

public final class CottontailEntityCreator implements EntityCreator {

  public static final String COTTONTAIL_PREFIX = "cottontail";
  public static final String INDEX_HINT = COTTONTAIL_PREFIX + ".index";

  /**
   * Internal reference to the {@link CottontailWrapper} used by this {@link CottontailEntityCreator}.
   */
  private final CottontailWrapper cottontail;

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
      final ListSchemas list = new ListSchemas().txId(txId);
      final TupleIterator iterator = this.cottontail.client.list(list);
      boolean exists = false;
      while (iterator.hasNext()) {
        Tuple next = iterator.next();
        if (Objects.equals(next.asString(Constants.COLUMN_NAME_DBO), CottontailWrapper.FQN_CINEAST_SCHEMA)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        this.cottontail.client.create(new CreateSchema(CottontailWrapper.CINEAST_SCHEMA).txId(txId));
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
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + TagReader.TAG_ENTITY_NAME;
      final CreateEntity create = new CreateEntity(fqn)
          .column(TagReader.TAG_ID_COLUMNNAME, Type.STRING, -1, false)
          .column(TagReader.TAG_NAME_COLUMNNAME, Type.STRING, -1, false)
          .column(TagReader.TAG_DESCRIPTION_COLUMNNAME, Type.STRING, -1, false)
          .txId(txId);
      this.cottontail.client.create(create);

      /* tag ids should be unique */
      this.createIndex(TagReader.TAG_ENTITY_NAME, TagReader.TAG_ID_COLUMNNAME, IndexType.BTREE_UQ, txId);
      /* tag names do not necessarily have to be unique */
      this.createIndex(TagReader.TAG_ENTITY_NAME, TagReader.TAG_NAME_COLUMNNAME, IndexType.BTREE, txId);
      /* could be used for autocomplete */
      this.createIndex(TagReader.TAG_ENTITY_NAME, TagReader.TAG_NAME_COLUMNNAME, IndexType.LUCENE, txId);

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
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + MediaObjectDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(fqn)
          .column(MediaObjectDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[1], Type.INTEGER, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaObjectDescriptor.FIELDNAMES[3], Type.STRING, -1, false)
          .txId(txId);
      this.cottontail.client.create(entity);

      /* Create index. */
      this.createIndex(MediaObjectDescriptor.ENTITY, MediaObjectDescriptor.FIELDNAMES[0], IndexType.BTREE_UQ, txId);
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
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + MediaSegmentDescriptor.ENTITY;
      final CreateEntity entity = new CreateEntity(fqn)
          .column(MediaSegmentDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[2], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[3], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[4], Type.INTEGER, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[5], Type.DOUBLE, -1, false)
          .column(MediaSegmentDescriptor.FIELDNAMES[6], Type.DOUBLE, -1, false)
          .txId(txId);
      this.cottontail.client.create(entity);

      /* Create indexes. */
      this.createIndex(MediaSegmentDescriptor.ENTITY, MediaSegmentDescriptor.FIELDNAMES[0], IndexType.BTREE_UQ, txId);
      this.createIndex(MediaSegmentDescriptor.ENTITY, MediaSegmentDescriptor.FIELDNAMES[1], IndexType.BTREE, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createMetadataEntity(String tableName) {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + tableName;
      final CreateEntity entity = new CreateEntity(fqn)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaObjectMetadataDescriptor.FIELDNAMES[3], Type.STRING, -1, false)
          .txId(txId);
      this.cottontail.client.create(entity);

      /* Create Index. */
      this.createIndex(tableName, MediaObjectMetadataDescriptor.FIELDNAMES[0], IndexType.BTREE, txId);
      this.createIndex(tableName, MediaObjectMetadataDescriptor.FIELDNAMES[2], IndexType.BTREE, txId);
      this.cottontail.client.commit(txId);
      return true;
    } catch (StatusRuntimeException e) {
      this.cottontail.client.rollback(txId);
      return false;
    }
  }

  @Override
  public boolean createSegmentMetadataEntity(String tableName) {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + tableName;
      final CreateEntity entity = new CreateEntity(fqn)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[0], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[1], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[2], Type.STRING, -1, false)
          .column(MediaSegmentMetadataDescriptor.FIELDNAMES[3], Type.STRING, -1, false)
          .txId(txId);
      this.cottontail.client.create(entity);

      /* Create Index. */
      this.createIndex(tableName, MediaSegmentMetadataDescriptor.FIELDNAMES[0], IndexType.BTREE, txId);
      this.createIndex(tableName, MediaSegmentMetadataDescriptor.FIELDNAMES[2], IndexType.BTREE, txId);
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
    var ent = this.createEntity(featureEntityName, extended);
    long txId = this.cottontail.client.begin();
    boolean success = this.createIndex(featureEntityName, GENERIC_ID_COLUMN_QUALIFIER, IndexType.BTREE, txId);
    if (success) {
      this.cottontail.client.commit(txId);
    } else {
      this.cottontail.client.rollback(txId);
    }
    return ent;
  }

  @Override
  public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
    final AttributeDefinition[] extended = new AttributeDefinition[attributes.length + 1];
    extended[0] = new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING);
    System.arraycopy(attributes, 0, extended, 1, attributes.length);
    var ent = this.createEntity(entityName, extended);
    long txId = this.cottontail.client.begin();
    boolean success = this.createIndex(entityName, GENERIC_ID_COLUMN_QUALIFIER, IndexType.BTREE, txId);
    if (success) {
      this.cottontail.client.commit(txId);
    } else {
      this.cottontail.client.rollback(txId);
    }
    return ent;
  }

  @Override
  public boolean createEntity(String entityName, AttributeDefinition... attributes) {
    return this.createEntity(new EntityDefinitionBuilder(entityName).withAttributes(attributes).build());
  }

  @Override
  public boolean createEntity(org.vitrivr.cineast.core.db.setup.EntityDefinition def) {
    final long txId = this.cottontail.client.begin();
    try {
      /* Create entity. */
      final String fqn = CottontailWrapper.CINEAST_SCHEMA + "." + def.getEntityName();
      final CreateEntity entity = new CreateEntity(fqn).txId(txId);
      for (AttributeDefinition attribute : def.getAttributes()) {
        int length = -1;
        if ((attribute.getType() == VECTOR || attribute.getType() == BITSET) && attribute.getLength() > 0) {
          length = attribute.getLength();
        }
        entity.column(attribute.getName(), mapAttributeType(attribute.getType()), length, false);
      }
      this.cottontail.client.create(entity);

      /* Create Index. */
      for (AttributeDefinition attribute : def.getAttributes()) {
        if (attribute.getType() == TEXT) {
          this.createIndex(def.getEntityName(), attribute.getName(), IndexType.LUCENE, txId);
        }
        // TODO (LS, 18.11.2020) Shouldn't we also have abstract indices in the db abstraction layer?
        final Optional<String> hint = attribute.getHint(INDEX_HINT);
        if (hint.isPresent()) {
          IndexType idx = IndexType.valueOf(hint.get());
          this.createIndex(def.getEntityName(), attribute.getName(), idx, txId);
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
      this.createIndex(entityName, column, IndexType.BTREE, txId);
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
      final TupleIterator results = this.cottontail.client.about(about);
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
      this.cottontail.client.drop(new DropEntity(fqn).txId(txId));
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
        return Type.BOOLEAN_VECTOR;
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


  private boolean createIndex(String entityName, String attribute, IndexType type, long txId) {
    var fqn = CottontailWrapper.CINEAST_SCHEMA + "." + entityName;
    final CreateIndex index = new CreateIndex(fqn, attribute, type).txId(txId);
    try {
      this.cottontail.client.create(index);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Index on entity {}, attribute {}, type {} was not created because it already exists", entityName, attribute, type);
        return false;
      }
      throw e;
    }
    return true;
  }
}
