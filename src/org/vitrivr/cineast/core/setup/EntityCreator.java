package org.vitrivr.cineast.core.setup;

import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;

public interface EntityCreator {

  String CINEAST_SEGMENT = "cineast_segment";
  String CINEAST_MULTIMEDIAOBJECT = "cineast_multimediaobject";

  /**
   * Initialises the main entity holding information about mutlimedia objects
   */
  AckMessage createMultiMediaObjectsEntity();

  /**
   * Initialises the entity responsible for holding information about segments of a mutlimedia object
   */
  AckMessage createSegmentEntity();

  /**
   * Initialises an entity for a feature module with default parameters
   * @param featurename the name of the feature module
   * @param unique true if the feature module produces at most one vector per segment
   */
  AckMessage createFeatureEntity(String featurename, boolean unique);

  AckMessage createFeatureEntity(String featurename, boolean unique, String... featrueNames);

  AckMessage createFeatureEntity(String featurename, boolean unique,
      AttributeDefinition... attributes);

  AckMessage createIdEntity(String entityName, AttributeDefinition... attributes);

  boolean existsEntity(String entityName);

  void close();

}