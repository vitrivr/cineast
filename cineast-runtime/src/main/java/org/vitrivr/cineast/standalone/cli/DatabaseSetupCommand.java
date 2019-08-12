package org.vitrivr.cineast.standalone.cli;


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.Collection;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * A CLI command that can be used to setup all the database entities required by Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "setup", description = "Makes the necessary database setup for Cineast and creates all the required entities.")
public class DatabaseSetupCommand implements Runnable {

    @Option(name = {"-c", "--clean"}, description = "Performs a cleanup before starting the setup; i.e. explicitly drops all entities.")
    private boolean clean = false;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void run() {
        final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
        if (ec != null) {
            /* Collects all the relevant retriever classes based on the application config. */
            final HashSet<Retriever> retrievers = new HashSet<>();
            for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
                retrievers.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
            }

            LOGGER.debug(clean);
            if (this.clean) {
                this.dropAllEntities(ec, retrievers);
            }

            LOGGER.info("Setting up basic entities...");

            ec.createMultiMediaObjectsEntity();
            ec.createMetadataEntity();
            ec.createSegmentMetadataEntity();
            ec.createSegmentEntity();
            ec.createTagEntity();

            LOGGER.info("...done");

            LOGGER.info("Setting up retriever classes...");

            for (Retriever r : retrievers) {
                LOGGER.info("Creating entity for {}", r.getClass().getSimpleName());
                r.initalizePersistentLayer(() -> ec);
            }
            LOGGER.info("...done");

            LOGGER.info("Setup complete!");

            /* Closes the EntityCreator. */
            ec.close();
        }
    }

    /**
     * Drops all entities currently required by Cineast.
     *
     * @param ec The {@link EntityCreator} used to drop the entities.
     * @param retrievers The list of {@link Retriever} classes to drop the entities for.
     */
    private void dropAllEntities(EntityCreator ec, Collection<Retriever> retrievers) {
        LOGGER.warn("Dropping all entities... ");
        ec.dropMultiMediaObjectsEntity();
        ec.dropMetadataEntity();
        ec.dropSegmentEntity();
        ec.dropSegmentMetadataEntity();
        ec.dropTagEntity();

        for (Retriever r : retrievers) {
            LOGGER.info("Dropping {}", r.getClass().getSimpleName());
            r.dropPersistentLayer(() -> ec);
        }
    }
}
