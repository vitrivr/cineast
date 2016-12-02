package org.vitrivr.cineast.core.setup;

public class NoEntityCreator implements EntityCreator {

  @Override
  public boolean createMultiMediaObjectsEntity() {
    return false;
  }

  @Override
  public boolean createSegmentEntity() {
    return false;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique) {
    return false;
  }

  @Override
  public boolean createFeatureEntity(String featurename, boolean unique, String... featrueNames) {
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
  public boolean existsEntity(String entityName) {
    return false;
  }

  @Override
  public void close() {
  }

}
