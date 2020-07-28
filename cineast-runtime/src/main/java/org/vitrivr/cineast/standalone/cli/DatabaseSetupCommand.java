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

  private static final Logger LOGGER = LogManager.getLogger(DatabaseSetupCommand.class);

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

  /**
   * This is a hacky way to support CLI and non CLI usage.
   *
   * TL;DR The functionality of this class is used in a non-cottontail configuration when clean-before-import is used.
   * See {@link org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler} for further explanation.
   */
  private final boolean isNotCommand;

  /**
   * For CLI
   */
  public DatabaseSetupCommand(){
    this(false);
  }

  /**
   * Other usages, i.e. in Import
   * @param nonCli
   */
  public DatabaseSetupCommand(boolean nonCli){
    this.isNotCommand = nonCli;
  }

  @Override
  public void run() {
    doSetup();
  }


  /**
   * Performs necessary setup on database, i.e. creating the required tables.
   */
  public void doSetup(){
    final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (ec != null) {
      HashSet<PersistentOperator> persistentOperators;
      if (this.extractionConfig == null) {
        persistentOperators = this.configPersistentOperators();
      } else {
        persistentOperators = this.extractionConfigPersistentOperators(this.extractionConfig);
      }

      if (this.clean) {
        this.dropAllEntities(ec, persistentOperators);
      }

      print("Setting up basic entities...");

      ec.createMultiMediaObjectsEntity();
      ec.createMetadataEntity();
      ec.createSegmentMetadataEntity();
      ec.createSegmentEntity();
      ec.createTagEntity();

      print("...done");

      print("Setting up retriever classes...");

      for (PersistentOperator r : persistentOperators) {
        print("Creating entity for " + r.getClass().getSimpleName());
        r.initalizePersistentLayer(() -> ec);
      }
      print("...done");

      print("Setup complete!");

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
    print("Dropping all entities... ");
    ec.dropMultiMediaObjectsEntity();
    ec.dropMetadataEntity();
    ec.dropSegmentEntity();
    ec.dropSegmentMetadataEntity();
    ec.dropTagEntity();

    for (PersistentOperator p : persistentOperators) {
      print("Dropping " + p.getClass().getSimpleName());
      p.dropPersistentLayer(() -> ec);
    }
  }

  /**
   * Prints the message given, if this is object was created in a CLI env, then it prints to standard out, otherwise it just logs on INFO
   */
  private void print(String msg){
    if(this.isNotCommand){
      LOGGER.info(msg);
    }else{
      System.out.println(msg);
    }
  }
}
