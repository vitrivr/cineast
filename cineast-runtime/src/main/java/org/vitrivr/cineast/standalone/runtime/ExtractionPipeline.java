package org.vitrivr.cineast.standalone.runtime;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.config.ExtractionPipelineConfig;
import org.vitrivr.cineast.standalone.monitoring.PrometheusExtractionTaskMonitor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.01.17
 */
public class ExtractionPipeline implements Runnable, ExecutionTimeCounter {


    private static final Logger LOGGER = LogManager.getLogger();

    /** The list of Extractor's that should be executed. */
    private final List<Extractor> extractors = new LinkedList<>();
    
    /** Blocking queue holding the SegmentContainers that are pending extraction. */
    private final LinkedBlockingQueue<SegmentContainer> segmentQueue;

    /** HashMap containing statistics about the execution of the extractors. */
    private final ConcurrentHashMap<String, SummaryStatistics> timeMap = new ConcurrentHashMap<>();

    /** ExecutorService used do execute the ExtractionTasks. */
    private final ExecutorService executorService;

    /** ExtractionContextProvider used to setup the Pipeline. It contains information about the Extractors. */
    private final ExtractionContextProvider context;
    
    /** Flag indicating whether or not the ExtractionPipeline is running. */
    private volatile boolean running = false;

    /**
     * Default constructor.
     *
     * @param context ExtractionContextProvider used to setup the pipeline.
     */
    public ExtractionPipeline(ExtractionContextProvider context) {
        /* Store context for further reference. */
        this.context = context;

        /* Start the extraction pipeline. */
        this.startup();

        /* Get value for size task-queue and number of threads. */
        int taskQueueSize =  context.taskQueueSize() > 0 ? context.taskQueueSize() : ExtractionPipelineConfig.DEFAULT_TASKQUEUE_SIZE;
        int threadCount = context.threadPoolSize() > 0 ? context.threadPoolSize() : ExtractionPipelineConfig.DEFAULT_THREADPOOL_SIZE;
        int segmentQueueSize = context.segmentQueueSize() > 0 ? context.segmentQueueSize() : ExtractionPipelineConfig.DEFAULT_SEGMENTQUEUE_SIZE;

        /* Initialize the segment queue. */
        this.segmentQueue = new LinkedBlockingQueue<>(segmentQueueSize);

        /* Prepare and create a new ThreadPoolExecutor. */
        LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(taskQueueSize);
        this.executorService = new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS, taskQueue){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if(t != null){
                    LOGGER.fatal("Decoding Error detected!");
                    LOGGER.fatal(LogHelper.getStackTrace(t));
                    //this.shutdownNow();
                }
                super.afterExecute(r, null);
            }
        };
    }

    /**
     * Sets the running flag to false which will halt the execution of the ExtractionPipeline as soon
     * as possible. Even when halted, there might still be ExtractionTasks in the executor that have not
     * finished yet!
     */
    public synchronized void stop() {
        this.running = false;
    }

    /**
     * Indicates whether or not the ExtractionPipeline is still running i.e. the while loop in the run()
     * method is still being executed. Even if this flag is set to false, there might still be ExtractionTasks
     * in the executor that have not finished yet!
     *
     * @return True if the ExtractionPipeline is running, false otherwise.
     */
    public synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Can be used to emit a SegmentContainer into the ExtractionPipeline. Invoking this method will add
     * the container to the local queue.
     *
     * If that queue is full, this method will block for the specified amount of time or until
     * space to becomes available. If during that time, no space becomes available, the method
     * returns false. This is an indication that emission of the segment should be retried later.
     *
     * @param container SegmentContainer to add to the queue.
     * @param timeout Time to wait for space to become available in ms.
     * @return true if SegmentContainer was emitted, false otherwise.
     *
     * @throws InterruptedException
     */
    public boolean emit(SegmentContainer container, int timeout) throws InterruptedException {
        return this.segmentQueue.offer(container, timeout,  TimeUnit.MILLISECONDS);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        /* Set running flag to true. */
        synchronized (this) { this.running = true; }

        /* Process SegmentContainers in Queue: For each Extractor in list dispatch an extraction task. */
        while (this.isRunning() || !this.segmentQueue.isEmpty()) {
            if(!this.isRunning()){
                LOGGER.debug("Received stop signal, still {} elements left", this.segmentQueue.size());
            }
            try {
                SegmentContainer s = this.segmentQueue.poll(500, TimeUnit.MILLISECONDS);
                if (s != null) {
                    LOGGER.debug("Segment {} is being handed to the extraction pipeline.", s.getId());
                    for (Extractor f : extractors) {
                        try {
                            this.executorService.execute(new ExtractionTask(f, s, this));
                            LOGGER.debug("Submitted segment {} for feature {}", s.getId(), f.getClass().getSimpleName());
                        } catch (RejectedExecutionException e) {
                            this.segmentQueue.clear();
                            LOGGER.fatal("Failed to submit segment {} for feature {}. Aborting...\n{}", s.getId(), f.getClass().getSimpleName(), LogHelper.getStackTrace(e));
                            break;
                        }
                    }

                    /* Sort list of extractors by execution time. */
                    (this.extractors).sort((o1,o2) -> Long.compare(getAverageExecutionTime(o2.getClass().getSimpleName()), getAverageExecutionTime(o1.getClass().getSimpleName())));
                }
            } catch (InterruptedException e) {
                LOGGER.warn("ShotDispatcher was interrupted: {}", LogHelper.getStackTrace(e));
            } 
        }

        this.shutdown();

        synchronized (this) { this.running = false; }
    }

    /**
     * Starts the ExtractionPipeline by initializing the Extractors.
     */
    private void startup() {
        LOGGER.info("Warming up extraction pipeline....");

        for (Extractor extractor : this.context.extractors()) {
            try {
                extractor.init(this.context.persistencyWriter(), this.context.batchSize());
                this.extractors.add(extractor);
            } catch (Throwable t) {
                LOGGER.warn("Failed to initialize extractor {} due to an exception: {}", extractor.getClass().getSimpleName(), t.getMessage());
            }
        }

        for (Extractor exporter : this.context.exporters()) {
            try {
                exporter.init(this.context.persistencyWriter(), this.context.batchSize());
                this.extractors.add(exporter);
            } catch (Throwable t) {
                LOGGER.warn("Failed to exporter extractor {} due to an exception: {}", exporter.getClass().getSimpleName(), t.getMessage());
            }
        }
        
        LOGGER.info("Extraction pipeline is ready with {} extractors.", this.extractors.size());
    }

    /**
     * Shuts the ExtractionPipeline down. Stops the ExecutorService and finishes the
     * extractors.
     */
    private void shutdown() {
        try {
            LOGGER.debug("Shutting down executor service");
            this.executorService.shutdown();
            LOGGER.debug("Waiting for termination of executor");
            this.executorService.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for Executor to shut down!");
        } finally {
            for (Extractor extractor : this.extractors) {
                try {
                    extractor.finish();
                } catch (Exception e) {
                    LOGGER.error("Error while shutting down extractor {} : {}",
                        extractor.getClass().getSimpleName(), LogHelper.getStackTrace(e));
                }
            }
            LOGGER.debug("All extractors termination, executor service terminated. Exiting shutdown.");
        }
    }

    @Override
    public void reportExecutionTime(String className, long milliseconds) {
        if(!this.timeMap.containsKey(className)){
            this.timeMap.put(className, new SummaryStatistics());
        }
        SummaryStatistics stat = this.timeMap.get(className);
          synchronized (stat) {
              stat.addValue(milliseconds);
          }
        PrometheusExtractionTaskMonitor.reportExecutionTime(className, milliseconds);
    }

    /**
     * @return the average execution time for all tasks reported for this class or 0 if the class is unknown or null
     */
    @Override
    public long getAverageExecutionTime(String className) {
        if(this.timeMap.containsKey(className)){
            return (long) this.timeMap.get(className).getMean();
        }
        return 0;
    }
}
