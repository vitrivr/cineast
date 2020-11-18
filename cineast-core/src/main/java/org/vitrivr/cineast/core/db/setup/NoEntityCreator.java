package org.vitrivr.cineast.core.db.setup;

import java.util.HashMap;

public class NoEntityCreator implements EntityCreator {


  @Override
  public boolean createMultiMediaObjectsEntity() {
    return false;
  }

  @Override
  public boolean createMetadataEntity() {
    return false;
  }

  @Override
  public boolean createSegmentMetadataEntity() { return false; }

  @Override
  public boolean createSegmentEntity() {
    return false;
  }

  @Override
  public boolean dropMultiMediaObjectsEntity() {
    return false;
  }

  @Override
  public boolean dropSegmentEntity() {
    return false;
  }

  @Override
  public boolean dropMetadataEntity() {
    return false;
  }

  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique, int length,
      String... featureNames) {
    return false;
  }


  @Override
  public boolean createFeatureEntity(String featureEntityName, boolean unique,
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
  public boolean createEntity(EntityDefinition entityDefinition) {
    return false;
  }

  @Override
  public boolean existsEntity(String entityName) {
    return false;
  }

  @Override
  public void close() {
  }

  @Override
  public boolean dropEntity(String entityName) {
    return false;
  }

  @Override
  public boolean createHashNonUniqueIndex(String entityName, String column) {
    return false;
  }

}
