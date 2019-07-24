package org.vitrivr.cineast.core.benchmark.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.benchmark.model.Benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class that can be used to write the Benchmarks captured in a Benchmark-Engine to disk. Currently,
 * only a CSV format is supported.
 *
 * TODO #1: Create different Formatters
 * TODO #2: File rotation
 *
 * @author rgasser
 * @version 1.0
 * @created 26.04.17
 */
public class BenchmarkWriter implements Runnable {
    /** LogManager instance used to log Benchmark results. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Delimiter used to separate entries. */
    private static final String DELIMITER_COLUMNS = ",";

    /** BenchmarkEngine that has been assigned to the current instance of BenchmarkWriter. */
    private final BenchmarkEngine engine;

    /** Path to the destination folder. */
    private final Path destination;

    /** BufferedWriter used to write Benchmark entries to a file. */
    private BufferedWriter writer;

    /**
     * Constructor for BenchmarkWriter.
     *
     * @param engine
     * @param destination
     */
    public BenchmarkWriter(BenchmarkEngine engine, Path destination) {
        this.engine = engine;
        this.destination = destination.resolve(engine.getName() + ".csv");
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
        /* Initializes the necessary files and writers. */
        if (!this.initialize()) {
          return;
        }

        /* Timeout between flushing the benchmark-queue. */
        int timeout = 30000;

        /*
         * Loop: Writes the content of the BenchmarkEngine to disk at regular intervals.
         */
        while(true) {
            /* Writes the latest Benchmark entries to disk. */
            this.write();

            /* Sets timeout. */
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOGGER.info("Thread was interrupted. BenchmarkWriter is being stopped...");
                break;
            }
        }

        /* Issues one final write. */
        this.write();

        /* Relinquishes resources like file-handles. */
        this.teardown();
    }

    /**
     * Initializes the Buffered writer and creates the file, if necessary.
     *
     * @return True if initialization succeeded, false otherwise.
     */
    private boolean initialize() {
        /* Create intermediate directories, if necessary. */
        if (!Files.exists(this.destination.getParent())) {
            try {
                Files.createDirectories(this.destination.getParent());
            } catch (IOException exception) {
                LOGGER.fatal("Could not create intermediate-folders to benchmark file.", this.destination);
                return false;
            }
        }

        /* Opens or creates (if necessary) the benchmark file */
        try {
            this.writer = Files.newBufferedWriter(this.destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            LOGGER.fatal("Could not open/create benchmark file.", this.destination);
            return false;
        }

        return true;
    }

    /**
     * Writes enqueued Benchmark objects to a file and returns the number of objects, that
     * have been written.
     *
     * @return Number of objects that have been written.
     */
    private int write() {
        List<Benchmark> benchmarks = this.engine.drain();
        int counter = 0;

        try {
            for (Benchmark benchmark : benchmarks) {
                Iterator<Map.Entry<String,Object>> iterator = benchmark.data().entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<String,Object> data = iterator.next();
                    this.writer.append(data.getValue().toString());
                    if (iterator.hasNext()) {
                      this.writer.append(DELIMITER_COLUMNS);
                    }
                }
                this.writer.newLine();
                counter += 1;
            }
            this.writer.flush();
        } catch (IOException e) {
            LOGGER.fatal("Error while writing Benchmark to file ({}/{} written).", counter, benchmarks.size());
            return counter;

        }
        return counter;
    }

    /**
     * Tears the BenchmarkWriter down and closes the BufferedWriter, thereby releasing
     * all associated handles and resources.
     */
    private void teardown() {
        try {
            this.writer.close();
            this.writer = null;
        } catch (IOException e) {
            LOGGER.fatal("Failed to close writer upon teardown of the class!");
        }
    }

    /**
     * Just to be safe; close writer again upon finalization in case
     * it failed in the teardown method.
     */
    @Override
    public void finalize() {
        try {
            if (this.writer != null) {
                this.writer.close();
                this.writer = null;
            }
        } catch (IOException e) {
            LOGGER.fatal("Failed to close writer upon finalisation!");
        }
    }
}
