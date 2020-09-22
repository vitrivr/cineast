package org.vitrivr.cineast.standalone.cli;


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;

import org.vitrivr.cineast.core.db.PersistentOperator;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ExtractorConfig;
import org.vitrivr.cineast.standalone.config.IngestConfig;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.path.ExtractionContainerProviderFactory;

/**
 * A CLI command that can be used to setup all the database entities required by Cineast.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "setup", description = "Makes the necessary database setup for Cineast and creates all the required entities and inidices.")
public class DatabaseSetupCommand implements Runnable {

  @Option(name = {"-c", "--clean"}, description = "Performs a cleanup before starting the setup; i.e. explicitly drops all entities.")
  private boolean clean = false;

  @Option(name = {"-e", "--extraction"}, title = "Extraction config", description = "If specified, setup database based upon Cineast extraction config file.")
  private String extractionConfig = null;

  private HashSet<PersistentOperator> configPersistentOperators() {
    /* Collects all the relevant retriever classes based on the application config. */
    final HashSet<PersistentOperator> persistentOperators = new HashSet<>();
    for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
      persistentOperators.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
    }
    return persistentOperators;
  }

  private HashSet<PersistentOperator> extractionConfigPersistentOperators(String extractionConfig) {
    final HashSet<PersistentOperator> persistentOperators = new HashSet<>();
    final File file = new File(extractionConfig);
    if (file.exists()) {
      final JacksonJsonProvider reader = new JacksonJsonProvider();
      final IngestConfig context = reader.toObject(file, IngestConfig.class);
      for (ExtractorConfig extractor : context.getExtractors()) {
        persistentOperators.add(extractor.getExtractor());
      }
    } else {
      System.err.println(String.format("Could not setup database based upon extraction config '%s'; the file does not exist!", file.toString()));
    }
    return persistentOperators;
}

  @Override
  public void run() {
    final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (ec != null) {
      HashSet<PersistentOperator> persistentOperators;
      if (this.extractionConfig == null) {
        persistentOperators = this.configPersistentOperators();
      } else {
        persistentOperators = this.extractionConfigPersistentOperators(this.extractionConfig);
      }

      System.out.println(clean);
      if (this.clean) {
        this.dropAllEntities(ec, persistentOperators);
      }

      System.out.println("Setting up basic entities...");

      ec.createMultiMediaObjectsEntity();
      ec.createMetadataEntity();
      ec.createSegmentMetadataEntity();
      ec.createSegmentEntity();
      ec.createTagEntity();

      System.out.println("...done");

      System.out.println("Setting up retriever classes...");

      for (PersistentOperator r : persistentOperators) {
        System.out.println("Creating entity for " + r.getClass().getSimpleName());
        r.initalizePersistentLayer(() -> ec);
      }
      System.out.println("...done");

      System.out.println("Setup complete!");

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
  private void dropAllEntities(EntityCreator ec, Collection<PersistentOperator> persistentOperators) {
    System.out.println("Dropping all entities... ");
    ec.dropMultiMediaObjectsEntity();
    ec.dropMetadataEntity();
    ec.dropSegmentEntity();
    ec.dropSegmentMetadataEntity();
    ec.dropTagEntity();

    for (PersistentOperator p : persistentOperators) {
      System.out.println("Dropping " + p.getClass().getSimpleName());
      p.dropPersistentLayer(() -> ec);
    }
  }
}
