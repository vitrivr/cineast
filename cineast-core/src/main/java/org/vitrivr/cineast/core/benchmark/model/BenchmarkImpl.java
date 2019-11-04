package org.vitrivr.cineast.core.benchmark.model;

import org.vitrivr.cineast.core.data.Pair;

import java.util.*;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class BenchmarkImpl implements Benchmark {
    /** Date of the Benchmark. */
    protected final Date date;

    /** Timestamp of the beginning of the run (in ms). */
    protected final long start;

    /** Timestamp of the end of the run (in ms). */
    protected long end = -1;

    /** Name of the Benchmark. */
    protected final String name;

    /** Sequence number of the Benchmark. */
    protected final int run;

    /** List of named splits. */
    private final List<Pair<String,Long>> splits = new ArrayList<>();

    /** Boolean indicating, whether the Benchmark was aborted. */
    protected boolean aborted = false;

    /**
     * Constructor of BenchmarkImpl.
     *
     * @param name Name of the Benchmark. Usually refers to the classname.
     */
    public BenchmarkImpl(String name) {
        this(name, 1);
    }

    /**
     * Constructor of BenchmarkImpl.
     *
     * @param name Name of the Benchmark. Usually refers to the classname.
     * @param run Sequence number (n-th run of the same Benchmark)
     */
    public BenchmarkImpl(String name, int run) {
        this.date = new Date();
        this.start = System.currentTimeMillis();
        this.name = name;
        this.run = run;
    }

    /**
     * Completes the Benchmark and sets the end-timestamp.
     */
    @Override
    public void end() {
        this.end = System.currentTimeMillis();
    }

    /**
     * Aborts the Benchmark.
     */
    @Override
    public void abort() {
        this.end = System.currentTimeMillis();
        this.aborted = true;
    }

    /**
     * Registers a named split. A name can only be used once.
     *
     * @param name Name of the split.
     */
    @Override
    public void split(String name) {
        this.splits.add(new Pair<>(name, System.currentTimeMillis()));
    }

    /**
     * Returns the total elapsed of the Benchmark in seconds.
     *
     * @return Duration of the Benchmark.
     */
    @Override
    public float elapsed() {
        if (this.end == -1) {
            return this.duration(this.start, System.currentTimeMillis());
        } else {
            return this.duration(this.start, this.end);
        }
    }

    /**
     * Returns true if the Benchmark is still running and false otherwise.
     *
     * @return Status of the Benchmark.
     */
    @Override
    public boolean isRunning() {
        return this.end == -1;
    }

    /**
     * Returns the name of the Benchmark run.
     *
     * @return Name of the Benchmark.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the sequence number of the Benchmark, that is a number indicating
     * how often the same Benchmark has been repeated.
     *
     * @return Run number of the benchmark.
     */
    @Override
    public int getRun() {
        return this.run;
    }

    /**
     * Returns a Map that maps the split names to their duration.
     *
     * <strong>Important:</strong> When iterating over the map, the splits are supposed
     * to occur in the order they were created!
     *
     * @return Map of splits and associated durations.
     */
    @Override
    public Map<String,Float> splitDurations() {
        LinkedHashMap<String,Float> map = new LinkedHashMap<>();
        for (int i=0; i<splits.size(); i++) {
            long begin = this.splits.get(i).second;
            long end = (i==splits.size()-1) ? this.end : this.splits.get(i+1).second;
            map.put(this.splits.get(i).first, this.duration(begin, end));
        }
        return map;
    }

    /**
     * Returns a Map containing the data of the Benchmark. The fieldnames defined above
     * can be used to access that data. Furthermore, some fields may be related to named
     * splits. This implementation uses a LinkedHashMap, i.e. when iterating, the key-value pairs
     * are returned in order they were added.
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
    @Override
    public Map<String,Object> data() {
        /* Prepare TreeMap and add the default fields. */
        final LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        data.put(FIELD_DATE, this.date);
        data.put(FIELD_NAME, this.name);
        data.put(FIELD_RUN, this.run);
        data.put(FIELD_TOTAL, this.duration(this.start, this.end));
        data.put(FIELD_START, this.start);
        data.put(FIELD_END, this.end);

        /* Calculate the split durations and add them as well. */
        Map<String,Float> splits = this.splitDurations();
        for (Map.Entry<String,Float> split : splits.entrySet()) {
            data.put(split.getKey(), split.getValue());
        }

        return data;
    }

    /**
     * Calculates the duration in seconds between a start and an end timestamp. The
     * method demands that start < end.
     *
     * @param start Unix timestamp of the start.
     * @param end Unix timestamp of the end.
     * @return Duration in seconds.
     */
    protected final float duration(long start, long end) {
        if (start > end) {
          throw new IllegalArgumentException("Start timestamp must not be later than the end timestamp.");
        }
        return (end-start) / 1000.0f;
    }
}

