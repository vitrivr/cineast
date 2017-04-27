package org.vitrivr.cineast.core.benchmark.model;

import java.util.Map;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public interface Benchmark {

    String FIELD_DATE = "Date";
    String FIELD_NAME = "Name";
    String FIELD_RUN = "Run";
    String FIELD_TOTAL = "Total";
    String FIELD_START = "Start";
    String FIELD_END = "End";

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
     * Returns a Map containing the data of the Benchmark. The fieldnames defined above
     * can be used to access that data. Furthermore, some fields may be related to named
     * splits.
     *
     * <ol>
     *     <li>FIELD_DATE: Date of the Benchmark</li>
     *     <li>FIELD_NAME: Name of the Benchmark</li>
     *     <li>FIELD_RUN: Sequence number of the Benchmark</li>
     *     <li>FIELD_TOTAL: Total duration of the Benchmark in seconds</li>
     *     <li>FIELD_START: Start timestamp of the Benchmark.</li>
     *     <li>FIELD_END: End timestamp of the Benchmark.</li>
     * </ol>
     *
     * <strong>Important: </strong> The values are expected to implement a meaningful
     * toString() representation!
     *
     * @return Map with the Benchmark-data
     */
    Map<String,Object> data();
}
