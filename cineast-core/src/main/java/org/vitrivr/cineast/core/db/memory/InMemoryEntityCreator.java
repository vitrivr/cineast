package org.vitrivr.cineast.core.db.memory;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;

import javax.management.Attribute;
import java.util.stream.Collectors;

/**
 * Implementation of a Cineast {@link EntityCreator} on top of the {@link InMemoryStore}.
 *
 * @see InMemoryStore
 *
 * @author Ralph Gasser
 * @version 1.0
 */
public class InMemoryEntityCreator implements EntityCreator {

  private final InMemoryStore store = InMemoryStore.sharedInMemoryStore();

  @Override
  public boolean createMultiMediaObjectsEntity() {
    return this.store.createEntity(MediaObjectDescriptor.ENTITY,
        MediaObjectDescriptor.FIELDNAMES[0],
        MediaObjectDescriptor.FIELDNAMES[1],
        MediaObjectDescriptor.FIELDNAMES[2],
        MediaObjectDescriptor.FIELDNAMES[3]
    ).isPresent();
  }

  @Override
  public boolean createMetadataEntity() {
    return this.store.createEntity(MediaObjectMetadataDescriptor.ENTITY,
        MediaObjectMetadataDescriptor.FIELDNAMES[0],
        MediaObjectMetadataDescriptor.FIELDNAMES[1],
        MediaObjectMetadataDescriptor.FIELDNAMES[2],
        MediaObjectMetadataDescriptor.FIELDNAMES[3]
    ).isPresent();
  }

  @Override
  public boolean createSegmentMetadataEntity() {
    return this.store.createEntity(MediaSegmentMetadataDescriptor.ENTITY,
        MediaSegmentMetadataDescriptor.FIELDNAMES[0],
        MediaSegmentMetadataDescriptor.FIELDNAMES[1],
        MediaSegmentMetadataDescriptor.FIELDNAMES[2],
        MediaSegmentMetadataDescriptor.FIELDNAMES[3]
    ).isPresent();
  }

  @Override
  public boolean createSegmentEntity() {
    return this.store.createEntity(MediaSegmentDescriptor.ENTITY,
        MediaSegmentDescriptor.FIELDNAMES[0],
        MediaSegmentDescriptor.FIELDNAMES[1],
        MediaSegmentDescriptor.FIELDNAMES[2],
        MediaSegmentDescriptor.FIELDNAMES[3],
        MediaSegmentDescriptor.FIELDNAMES[4],
        MediaSegmentDescriptor.FIELDNAMES[5],
        MediaSegmentDescriptor.FIELDNAMES[6]
    ).isPresent();
  }

  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique, int length, String... featureNames) {
    final String[] columns = new String[featureNames.length + 1];
    columns[0] = GENERIC_ID_COLUMN_QUALIFIER;
    System.arraycopy(featureNames, 0, columns, 1, columns.length - 1);
    return this.store.createEntity(featureEntityName, columns).isPresent();
  }

  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique, AttributeDefinition... attributes) {
    return createIdEntity(featureEntityName, attributes);
  }

  @Override
  public boolean createIdEntity(String entityName, AttributeDefinition... attributes) {
    final String[] columns = new String[attributes.length + 1];
    columns[0] = GENERIC_ID_COLUMN_QUALIFIER;
    for (int i = 1; i<columns.length; i++) {
      columns[i] = attributes[i-1].getName();
    }
    return this.store.createEntity(entityName, columns).isPresent();
  }

  @Override
  public boolean createEntity(String entityName, AttributeDefinition... attributes) {
    return this.createEntity(new EntityDefinition.EntityDefinitionBuilder(entityName).withAttributes(attributes).build());
  }

  @Override
  public boolean createEntity(EntityDefinition entityDefinition) {
    return this.store.createEntity(entityDefinition.getEntityName(),
            entityDefinition.getAttributes().stream().map(AttributeDefinition::getName).toArray(String[]::new)).isPresent();
  }

  @Override
  public boolean existsEntity(String entityName) {
    return this.store.hasEntity(entityName);
  }

  @Override
  public boolean dropEntity(String entityName) {
    return this.store.dropEntity(entityName);
  }

  @Override
  public boolean createHashNonUniqueIndex(String entityName, String column) {
    /* Ignored for in-memory store*/
    return false;
  }

  @Override
  public void close() {

  }
}
