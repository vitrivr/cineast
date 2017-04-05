package org.vitrivr.cineast.core.importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author rgasser
 * @version 1.0
 * @created 01.03.17
 */
public class DataImportHandler {

  private class DataImportRunner implements Runnable {

    /** Path to the file */
    private String entityName;

    /** */
    private Importer<?> importer;

    /**
     *
     * @param path
     * @param importer
     */
    public DataImportRunner(Importer<?> importer, Path path) {
      String suffix = path.getFileName().toString()
          .substring(path.getFileName().toString().lastIndexOf("."));
      this.entityName = path.getFileName().toString().replace(suffix, "");
      this.importer = importer;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread,
     * starting the thread causes the object's <code>run</code> method to be called in that
     * separately executing thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action
     * whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
      try {
        Copier copier = new Copier(this.entityName, this.importer);
        LOGGER.info("Starting progress on: {}", this.entityName);
        copier.copyBatched(DataImportHandler.this.batchsize);
        LOGGER.info("Completed import of: {}", this.entityName);
      } catch (Exception e) {
        LOGGER.error("Error while copying data for '{}': {}", this.entityName,
            LogHelper.getStackTrace(e));
      }
    }
  }

  private static final Logger LOGGER = LogManager.getLogger();

  /** ExecutorService used for execution of the DataImportRunner. */
  private final ExecutorService service;

  /** Size of data batches (i.e. number if tuples) that are sent to the persistence layer. */
  private final int batchsize;

  /** */
  private final ArrayList<Future> futures = new ArrayList<>();

  /**
   * Default constructor; creates DataImportHandler with two threads and a batchsize of 100.
   */
  public DataImportHandler() {
    this(2, 100);
  }

  /**
   * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
   *
   * @param threads
   *          Number of threads to use for data import.
   * @param batchsize
   *          Size of data batches that are sent to the persistence layer.
   */
  public DataImportHandler(int threads, int batchsize) {
    this.service = Executors.newFixedThreadPool(threads);
    this.batchsize = batchsize;
  }

  /**
   * Starts data import process using JSON files. The method can either be used with a single file
   * or a folder containing JSON files.
   *
   * @param path
   *          Path to the JSON file or a folder containing JSON files.
   */
  public void importJson(Path path) {
    try {
      LOGGER.info("Starting data import with JSON files in: {}", path.toString());
      Files.walk(path, 2).forEach(p -> {
        try {
          if (p.toString().toLowerCase().endsWith(".json")) {
            this.futures.add(
                this.service.submit(new DataImportRunner(new JsonObjectImporter(p.toFile()), p)));
          }
        } catch (IOException e) {
          LOGGER.error("Could not start data import for file '{}'. Skipping...?", p.toString());
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with JSON files in: {}", path.toString());
      System.gc();
      
    } catch (IOException e)

    {
      LOGGER.error(
          "Could not start data import process with path '{}' due to an IOException: {}. Aborting...",
          path.toString(), LogHelper.getStackTrace(e));
    }

  }

  /**
   * Starts data import process using PROTO files. The method can either be used with a single file
   * or a folder containing PROTO files.
   *
   * @param path
   *          Path to the PROTO file or a folder containing PROTO files.
   */
  public void importProto(Path path) {
    try {
      LOGGER.info("Starting data import with PROTO files in: {}", path.toString());
      Files.walk(path, 2).forEach(p -> {
        try {
          if (p.toString().toLowerCase().endsWith(".bin")) {
            this.futures.add(this.service
                .submit(new DataImportRunner(new TupleInsertMessageImporter(p.toFile()), p)));
          }
        } catch (FileNotFoundException e) {
          LOGGER.error("Could not start data import for file '{}'. Skipping...?", p.toString());
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with PROTO files in: {}", path.toString());
    } catch (IOException e) {
      LOGGER.error(
          "Could not start data import process with path '{}' due to an IOException. Aborting...",
          path.toString());
    }
  }

  /**
   * Awaits completion of the individual Futures. This method blocks until all Futures have been
   * completed.
   */
  private void waitForCompletion() {
    this.futures.removeIf(f -> {
      try {
        return (f.get() == null);
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Execution of one of the tasks could not be completed!");
        return true;
      }
    });
  }
}
