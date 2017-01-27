package org.vitrivr.cineast.core.setup;

public interface EntityCreator {

  String CINEAST_SEGMENT = "cineast_segment";
  String CINEAST_MULTIMEDIAOBJECT = "cineast_multimediaobject";

  /**
   * Initialises the main entity holding information about mutlimedia objects
   */
  boolean createMultiMediaObjectsEntity();

  /**
   * Initialises the entity responsible for holding information about segments of a mutlimedia object
   */
  boolean createSegmentEntity();

  /**
   * Initialises an entity for a feature module with default parameters
   * @param featurename the name of the feature module
   * @param unique true if the feature module produces at most one vector per segment
   */
  boolean createFeatureEntity(String featurename, boolean unique);

  boolean createFeatureEntity(String featurename, boolean unique, String... featrueNames);

  boolean createFeatureEntity(String featurename, boolean unique,
      AttributeDefinition... attributes);

  boolean createIdEntity(String entityName, AttributeDefinition... attributes);

  boolean existsEntity(String entityName);

  /**
   * drops an entity, returns <code>true</code> if the entity was successfully dropped, <code>false</code> otherwise
   * @param entityName the entity to drop
   */
  boolean dropEntity(String entityName);
  
  void close();

}