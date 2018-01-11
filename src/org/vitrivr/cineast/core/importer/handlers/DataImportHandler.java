package org.vitrivr.cineast.core.importer.handlers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.importer.Copier;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 01.03.17
 */
public abstract class DataImportHandler {

    /**
     * This inner class implements the runnable that actually executes the copy operation.
     */
    protected class DataImportRunner implements Runnable {

        /** Name of the entity the {@link DataImportRunner} populates. */
        private String entityName;

        /** The {@link Importer} instance used to import the data. */
        private Importer<?> importer;

        /**
         *
         * @param importer
         * @param entityName
         */
        public DataImportRunner(Importer<?> importer, String entityName) {
            this.entityName = entityName;
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
                final Copier copier = new Copier(this.entityName, this.importer);
                LOGGER.info("Starting progress on: {}", this.entityName);
                copier.copyBatched(DataImportHandler.this.batchsize);
                LOGGER.info("Completed import of: {}", this.entityName);
            } catch (Exception e) {
                LOGGER.error("Error while copying data for '{}': {}", this.entityName, LogHelper.getStackTrace(e));
            }
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /** ExecutorService used for execution of the {@link DataImportRunner}. */
    protected final ExecutorService service;

    /** Size of data batches (i.e. number if tuples) that are sent to the persistence layer. */
    protected final int batchsize;

    /** */
    protected final ArrayList<Future<?>> futures = new ArrayList<>();

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public DataImportHandler(int threads, int batchsize) {
        this.service = Executors.newFixedThreadPool(threads);
        this.batchsize = batchsize;
    }

    /**
     * @param path
     */
    public abstract void doImport(Path path);

    /**
     * Awaits completion of the individual Futures. This method blocks until all Futures have been completed.
     */
    protected void waitForCompletion() {
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
