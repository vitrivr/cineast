package org.vitrivr.cineast.standalone.importer.handlers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.cli.DatabaseSetupCommand;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.Copier;
import org.vitrivr.cineast.standalone.monitoring.ImportTaskMonitor;
import org.vitrivr.cottontail.grpc.CottontailGrpc;

/**
 * @author rgasser
 * @version 1.0
 * @created 01.03.17
 */
public abstract class DataImportHandler {


    /**
     * Drops the entity if called. This is in order to have clean imports.
     * @param entityName The entity name to drop
     * @param taskName The task name during which this dropping occurs. Only for logging purposes
     */
    protected static void cleanOnDemand(String entityName, String taskName){
        final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
        /* Beware, this drops the table */
        CottontailGrpc.EntityDefinition entityDefinition = null;
        CottontailWrapper cottontail = null;
        if (Config.sharedConfig().getDatabase().getSelector() != DatabaseConfig.Selector.COTTONTAIL || Config.sharedConfig().getDatabase().getWriter() != DatabaseConfig.Writer.COTTONTAIL) {
            LOGGER.warn("Other database than cottontaildb in use. Using inconvenient database restore");
        }else{
            LOGGER.info("Storing entity ({}) details for re-setup", entityName);
            cottontail = new CottontailWrapper(Config.sharedConfig().getDatabase(), true);
            entityDefinition = cottontail.entityDetailsBlocking(CottontailWrapper.entityByName(entityName));
        }
        LOGGER.info("{} - Dropping table for entity {}...", taskName, entityName);
        ec.dropEntity(entityName);
        LOGGER.info("{} - Finished dropping table for entity {}", taskName, entityName);
        if(entityDefinition == null){
            LOGGER.warn("Calling command: setup -- This may take a while");
            DatabaseSetupCommand setupCmd = new DatabaseSetupCommand();
            setupCmd.doSetup();
        }else{
            cottontail.createEntityBlocking(entityDefinition);
            LOGGER.info("Re-created entity: {}", entityDefinition.getEntity().getName());
        }
    }

    protected static void createEntityOnDemand(EntityDefinition def, String taskName){
        LOGGER.debug("Creating entity: "+def);
        EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
        if(ec.existsEntity(def.getEntityName())){
            LOGGER.warn("Entity already exists, ignoring");
            return;
        }
        if(ec.createEntity(def)){
            LOGGER.info("Successfully created entity "+def);
        }else{
            LOGGER.error("Could not create entity "+def+" please see the log");
        }
    }



    /**
     * This inner class implements the runnable that actually executes the copy operation.
     */
    protected class DataImportRunner implements Runnable {

        /**
         * Name of the entity the {@link DataImportRunner} populates.
         */
        private final String entityName;

        /**
         * The {@link Importer} instance used to import the data.
         */
        private final Importer<?> importer;
        /**
         * A -possibly- human readable name for the import task
         */
        private final String taskName;

        /**
         * Whether or not the table of the entity to import should be dropped beforehand.
         * Basically, if this is true: its a TRUNCATE_EXISTING write, otherwise it's an APPEND write
         */
        private final boolean clean;

        /**
         * Creates a new {@link DataImportRunner} to run the import of the specified {@link Importer}.
         * If specified, drops the entity's table beforehand.
         *
         * @param importer   The {@link Importer} to run the import of
         * @param entityName The name of the entity to import into
         * @param taskName   The name of the task (possibly human readable)
         * @param clean      Whether to drop the entity's table beforehand or not
         */
        public DataImportRunner(Importer<?> importer, String entityName, String taskName, boolean clean) {
            this.entityName = entityName;
            this.importer = importer;
            this.taskName = taskName;
            this.clean = clean;
            if(clean){
                cleanOnDemand(this.entityName, this.taskName);
            }
        }

        /**
         * Creates a new {@link DataImportRunner} to run the import of the specified {@link Importer}.
         * Does an APPEND import, i.e. existing entries on that entity are kept.
         *
         * @param importer   The {@link Importer} to run the import of
         * @param entityName The name of the entity to import into
         * @param taskName   The name of the task (possibly human readable)
         */
        public DataImportRunner(Importer<?> importer, String entityName, String taskName) {
            this(importer, entityName, taskName, false);
        }

        /**
         * Creates a new import runner for given importer and creates the entity, if not existent.
         * Creates a new {@link DataImportRunner} to run the import of the specified {@link Importer}, while creating the entity before hand, using the given {@link EntityDefinition}.
         * @param importer The importer to run the import of
         * @param entity The entity definition of the entity to import. If not existent, the entity will be created first
         * @param taskName The name of the task, possibly human readable
         */
        public DataImportRunner(Importer<?> importer, EntityDefinition entity, String taskName){
            this(importer, entity.getEntityName(), taskName);
            createEntityOnDemand(entity, taskName);
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes the object's <code>run</code> method to be called in that separately executing thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                final Copier copier = new Copier(this.entityName, this.importer);
                LOGGER.info("Starting progress on entity: {}, task {}...", this.entityName, taskName);
                copier.copyBatched(DataImportHandler.this.batchsize);
                copier.close();
                LOGGER.info("Completed import of entity: {}, task {}", this.entityName, taskName);
                long stop = System.currentTimeMillis();
                ImportTaskMonitor.reportExecutionTime(taskName, stop - start);
                Thread.sleep(1_000);
            } catch (Exception e) {
                LOGGER.error("Error for task {} while copying data for '{}': {}", taskName, this.entityName, LogHelper.getStackTrace(e));
            }
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * ExecutorService used for execution of the {@link DataImportRunner}.
     */
    protected final ExecutorService service;

    /**
     * Size of data batches (i.e. number if tuples) that are sent to the persistence layer.
     */
    protected final int batchsize;

    protected int numberOfThreads;

    /**
     *
     */
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
        this.numberOfThreads = threads;
    }

    public abstract void doImport(Path path);

    /**
     * Awaits completion of the individual Futures. This method blocks until all Futures have been completed.
     */
    public void waitForCompletion() {
        this.futures.removeIf(f -> {
            try {
                Object o = f.get();
                if (o == null) {
                    return true;
                }
                LOGGER.warn("Future returned {}, still returning true", o);
                return true;
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Execution of one of the tasks could not be completed!");
                return true;
            }
        });
        try {
            this.futures.forEach(future -> {
                LOGGER.warn("A future is still present, this should not be happening.");
            });
            LOGGER.info("Shutting down threadpool for {}", this.getClass().getSimpleName());
            this.service.shutdown();
            LOGGER.info("Awaiting termination {}", this.getClass().getSimpleName());
            this.service.awaitTermination(30, TimeUnit.SECONDS);
            LOGGER.info("Service terminated {}", this.getClass().getSimpleName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
