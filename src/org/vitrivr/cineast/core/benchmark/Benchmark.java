package org.vitrivr.cineast.core.benchmark;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public interface Benchmark {
    /**
     * Returns the total duration of the Benchmark in seconds.
     *
     * @return Duration of the Benchmark.
     */
    float elapsed();

    /**
     * Registers a named split. A name can only be used once.
     *
     * @param name Name of the split.
     */
    void split(String name);

    /**
     * Completes the Benchmark and sets the end-timestamp.
     */
    void end();

    /**
     * Returns true if the Benchmark is still running and false otherwise.
     *
     * @return Status of the Benchmark.
     */
    boolean isRunning();

    /**
     * Returns the name of the Benchmark run.
     *
     * @return Name of the Benchmark.
     */
    String getName();

    /**
     * Returns the sequence number of the Benchmark, that is a number indicating
     * how often the same Benchmark has been repeated.
     *
     * @return Run number of the benchmark.
     */
    int getRun();

    /**
     *
     * @return
     */
    String toCSV();
}
