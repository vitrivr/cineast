package org.vitrivr.cineast.core.setup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.NeuralNetConfig;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.NeuralNetVGG16Feature;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import java.util.HashMap;
import java.util.HashSet;

public interface EntityCreator {

    /**
     * Logger instance used for logging.
     */
    Logger LOGGER = LogManager.getLogger();

    /**
     * Performs the setup of the persistent layer by executing all the necessary entity
     * creation steps in a sequence.
     *
     * @param options Optional options that can be provided for setup (currently not used). MUST NOT BE NULL!
     * @return boolean Indicating success or failure of the setup.
     */
    default boolean setup(HashMap<String, String> options) {
        LOGGER.info("Setting up basic entities...");

        this.createMultiMediaObjectsEntity();
        this.createMetadataEntity();
        this.createSegmentEntity();

        LOGGER.info("...done");


        LOGGER.info("Collecting retriever classes...");

        HashSet<Retriever> retrievers = new HashSet<>();
        for (String category : Config.getRetrieverConfig().getRetrieverCategories()) {
            retrievers.addAll(Config.getRetrieverConfig().getRetrieversByCategory(category).keySet());
        }

        LOGGER.info("...done");

        for (Retriever r : retrievers) {
            LOGGER.info("Setting up " + r.getClass().getSimpleName());
            r.initalizePersistentLayer(() -> this);
        }

        NeuralNetConfig nnconfig = Config.getNeuralNetConfig();
        if (nnconfig != null) {
            LOGGER.info("Initializing NeuralNet persistent layer...");
            NeuralNetFeature feature = new NeuralNetVGG16Feature(Config.getNeuralNetConfig());
            feature.initalizePersistentLayer(() -> this);
            LOGGER.info("...done");

            LOGGER.info("Initializing writer...");
            feature.init(Config.getDatabaseConfig().getWriterSupplier());
            feature.init(Config.getDatabaseConfig().getSelectorSupplier());
            LOGGER.info("...done");

            LOGGER.info("Filling labels...");
            feature.fillConcepts(Config.getNeuralNetConfig().getConceptsPath());
            feature.fillLabels(new HashMap<>());
            LOGGER.info("...done");
        } else {
            LOGGER.warn("No configuration for NeuralNet persistent layer found. Skipping...");
        }

        System.out.println("Setup complete!");

        return true;
    }


    /**
     * Initialises the main entity holding information about multimedia objects
     */
    boolean createMultiMediaObjectsEntity();

    /**
     * Initializes the entity responsible for holding metadata information about multimedia objects.
     */
    boolean createMetadataEntity();

    /**
     * Initializes the entity responsible for holding information about segments of a multimedia object
     */
    boolean createSegmentEntity();

    /**
     * Initializes an entity for a feature module with default parameters
     *
     * @param featurename the name of the feature module
     * @param unique      true if the feature module produces at most one vector per segment
     */
    boolean createFeatureEntity(String featurename, boolean unique);

    boolean createFeatureEntity(String featurename, boolean unique, String... featureNames);

    boolean createFeatureEntity(String featurename, boolean unique,
                                AttributeDefinition... attributes);

    boolean createIdEntity(String entityName, AttributeDefinition... attributes);

    boolean existsEntity(String entityName);

    void close();
}