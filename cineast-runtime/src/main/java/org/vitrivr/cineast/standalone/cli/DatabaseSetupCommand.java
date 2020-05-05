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
@Command(name = "setup", description = "Makes the necessary database setup for Cineast and creates all the required entities and inidices.")
public class DatabaseSetupCommand implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(DatabaseSetupCommand.class);

  @Option(name = {"-c", "--clean"}, description = "Performs a cleanup before starting the setup; i.e. explicitly drops all entities.")
  private boolean clean = false;

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
      /* Collects all the relevant retriever classes based on the application config. */
      final HashSet<Retriever> retrievers = new HashSet<>();
      for (String category : Config.sharedConfig().getRetriever().getRetrieverCategories()) {
        retrievers.addAll(Config.sharedConfig().getRetriever().getRetrieversByCategory(category).keySet());
      }

      if (this.clean) {
        this.dropAllEntities(ec, retrievers);
      }

      print("Setting up basic entities...");

      ec.createMultiMediaObjectsEntity();
      ec.createMetadataEntity();
      ec.createSegmentMetadataEntity();
      ec.createSegmentEntity();
      ec.createTagEntity();

      print("...done");

      print("Setting up retriever classes...");

      for (Retriever r : retrievers) {
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
  private void dropAllEntities(EntityCreator ec, Collection<Retriever> retrievers) {
    print("Dropping all entities... ");
    ec.dropMultiMediaObjectsEntity();
    ec.dropMetadataEntity();
    ec.dropSegmentEntity();
    ec.dropSegmentMetadataEntity();
    ec.dropTagEntity();

    for (Retriever r : retrievers) {
      print("Dropping " + r.getClass().getSimpleName());
      r.dropPersistentLayer(() -> ec);
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
