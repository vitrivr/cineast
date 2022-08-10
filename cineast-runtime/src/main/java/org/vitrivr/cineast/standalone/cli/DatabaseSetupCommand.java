package org.vitrivr.cineast.standalone.cli;


import com.carrotsearch.hppc.procedures.ObjectProcedure;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.PersistentOperator;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ExtractorConfig;
import org.vitrivr.cineast.standalone.config.IngestConfig;

/**
 * A CLI command that can be used to setup all the database entities required by Cineast.
 */
@Command(name = "setup", description = "Makes the necessary database setup for Cineast and creates all the required entities and inidices.")
public class DatabaseSetupCommand extends AbstractCineastCommand {

  private static final Logger LOGGER = LogManager.getLogger(DatabaseSetupCommand.class);
  /**
   * This is a hacky way to support CLI and non CLI usage.
   * <p>
   * TL;DR The functionality of this class is used in a non-cottontail configuration when clean-before-import is used. See {@link org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler} for further explanation.
   */
  private final boolean isNotCommand;
  @Option(name = {"-c", "--clean"}, description = "Performs a cleanup before starting the setup; i.e. explicitly drops all entities.")
  private boolean clean = false;
  @Option(name = {"-e", "--extraction"}, title = "Extraction config", description = "If specified, setup database based upon Cineast extraction config file.")
  private String extractionConfig = null;

  /**
   * For CLI
   */
  public DatabaseSetupCommand() {
    this(false);
  }

  /**
   * Other usages, i.e. in Import
   */
  public DatabaseSetupCommand(boolean nonCli) {
    this.isNotCommand = nonCli;
  }

  private HashSet<PersistentOperator> configPersistentOperators() {
    /* Collects all the relevant retriever classes based on the application config. */
    final HashSet<PersistentOperator> persistentOperators = new HashSet<>();
    for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
      Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keys().forEach((ObjectProcedure<? super Retriever>) persistentOperators::add);
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
      System.err.printf("Could not setup database based upon extraction config '%s'; the file does not exist!%n", file.getAbsolutePath());
    }
    return persistentOperators;
  }

  @Override
  public void execute() {
    doSetup();
  }


  /**
   * Performs necessary setup on database, i.e. creating the required tables.
   */
  public void doSetup() {
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
   * @param ec                  The {@link EntityCreator} used to drop the entities.
   * @param persistentOperators The list of {@link PersistentOperator} classes to drop the entities for.
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
  private void print(String msg) {
    if (this.isNotCommand) {
      LOGGER.info(msg);
    } else {
      System.out.println(msg);
    }
  }
}
