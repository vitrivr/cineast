package org.vitrivr.cineast.core.db.setup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;

import java.util.HashMap;
import java.util.Map;

public interface EntityCreator extends AutoCloseable {
    /**
     * Logger instance used for logging.
     */
    Logger LOGGER = LogManager.getLogger();

    /**
     * Name of the 'clean' flag.
     */
    String SETUP_FLAG_CLEAN = "clean";

    /**
     * Initialises the main entity holding information about multimedia objects
     */
    boolean createMultiMediaObjectsEntity();

    /**
     * Initialises the entity responsible for holding metadata information about multimedia objects.
     */
    boolean createMetadataEntity();

    /**
     * Initialises the entity responsible for holding metadata information about multimedia segments.
     */
    boolean createSegmentMetadataEntity();

    /**
     * Initialises the entity responsible for holding the mapping between human readable tags and their descriptions to the internally used ids
     */
    default boolean createTagEntity() {
      final Map<String, String> hints = new HashMap<>(1);
      hints.put("handler", "postgres");
      return this.createIdEntity(TagReader.TAG_ENTITY_NAME, new AttributeDefinition(TagReader.TAG_NAME_COLUMNNAME, AttributeType.STRING, hints), new AttributeDefinition(TagReader.TAG_DESCRIPTION_COLUMNNAME, AttributeType.STRING, hints));
    }


    /**
     * Initializes the entity responsible for holding information about segments of a multimedia object
     */
    boolean createSegmentEntity();

    /**
     * Drops the main entity holding information about multimedia objects
     */
    default boolean dropMultiMediaObjectsEntity() {
        if (this.dropEntity(MediaObjectDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped multimedia object entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping multimedia object entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding information about segments of a multimedia object
     */
    default boolean dropSegmentEntity() {
        if (this.dropEntity(MediaSegmentDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped segment entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping segment entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding metadata about segments of a multimedia object
     */
    default boolean dropSegmentMetadataEntity() {
        if (this.dropEntity(MediaSegmentMetadataDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped segment entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping segment entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding metadata information about multimedia objects.
     */
    default boolean dropMetadataEntity() {
        if (this.dropEntity(MediaObjectMetadataDescriptor.ENTITY)) {
            LOGGER.info("Successfully dropped metadata entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping metadata entity");
            return false;
        }
    }

    /**
     * Drops the entity responsible for holding metadata information about multimedia objects.
     */
    default boolean dropTagEntity() {
        if (this.dropEntity(TagReader.TAG_ENTITY_NAME)) {
            LOGGER.info("Successfully dropped tag entity.");
            return true;
        } else {
            LOGGER.error("Error occurred while dropping tag entity");
            return false;
        }
    }

    /**
     * Creates and initializes an entity for a feature module with default parameters
     *
     * @param featurename the name of the feature module
     * @param unique      true if the feature module produces at most one vector per segment
     */
    default boolean createFeatureEntity(String featurename, boolean unique, int length) {
        return createFeatureEntity(featurename, unique, length, "feature");
    }

    boolean createFeatureEntity(String featurename, boolean unique, int length, String... featureNames);

    boolean createFeatureEntity(String featurename, boolean unique, AttributeDefinition... attributes);

    /**
     * Creates and initializes an entity with the provided name and the provided attributes. The new entity will have an additional
     * field prepended, called "id", which is of type "string" and has an index.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    boolean createIdEntity(String entityName, AttributeDefinition... attributes);

    /**
     * Creates and initializes an entity with the provided name and the provided attributes.
     *
     * @param entityName Name of the new entity.
     * @param attributes List of {@link AttributeDefinition} objects specifying the new entities attributes.
     * @return True on success, false otherwise.
     */
    boolean createEntity(String entityName, AttributeDefinition... attributes);

    /**
     * @param entityName
     * @return
     */
    boolean existsEntity(String entityName);

    /**
     * drops an entity, returns <code>true</code> if the entity was successfully dropped, <code>false</code> otherwise
     *
     * @param entityName the entity to drop
     */
    boolean dropEntity(String entityName);

    @Override
    void close();
}