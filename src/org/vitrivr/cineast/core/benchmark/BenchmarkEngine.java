package org.vitrivr.cineast.core.benchmark;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public class BenchmarkEngine {
    /**
     * Implementation of the Benchmark-Interface.
     */
    private class BenchmarkImpl implements Benchmark {

        private final static String DELIMITER = "; ";

        /** Timestamp of the beginning of the run (in ms). */
        private final long start;

        /** Timestamp of the end of the run (in ms). */
        private long end = -1;

        /** Name of the Benchmark. */
        private final String name;

        /** Name of the Benchmark. */
        private final int run;

        /** List of named splits. */
        private final TObjectLongHashMap<String> splits = new TObjectLongHashMap<>();

        /** */

        /**
         *
         * @param name
         */
        private BenchmarkImpl(String name) {
            this(name, 1);
        }

        /**
         *
         * @param name
         * @param run
         */
        private BenchmarkImpl(String name, int run) {
            this.start = System.currentTimeMillis();
            this.name = name;
            this.run = run;
            BenchmarkEngine.LOGGER.debug("Beginning Benchmark for {}.", this.name);
        }

        /**
         * Returns the total elapsed of the Benchmark in seconds.
         *
         * @return Duration of the Benchmark.
         */
        @Override
        public float elapsed() {
            if (this.end == -1) {
                return (System.currentTimeMillis()-this.start) / 1000.0f;
            } else {
                return (this.end-this.start) / 1000.0f;
            }
        }

        /**
         * Ends the current Benchmark run and registers the time at which the
         * method was invoked.
         */
        public void end() {
            this.end = System.currentTimeMillis();
            BenchmarkEngine.LOGGER.debug("Ended Benchmark for {} (Elapsed: {}s).", this.name, this.elapsed());
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
         * Registers a named split. A name can only be used once.
         *
         * @param name Name of the split.
         */
        public void split(String name) {
            this.splits.put(name, System.currentTimeMillis());
            BenchmarkEngine.LOGGER.debug("Added split '{}' for benchmark '{}' (Elapsed: {}s).", name, this.name, this.elapsed());
        }

        /**
         *
         * @return
         */
        public String toCSV() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.name);
            builder.append(DELIMITER);
            builder.append(this.run);
            builder.append(DELIMITER);
            builder.append(this.start);
            builder.append(DELIMITER);

            this.splits.forEachEntry((k,v) -> {
                builder.append(k);
                builder.append(DELIMITER);
                builder.append(v);
                builder.append(DELIMITER);
                return false;
            });

            builder.append(this.end);
            return builder.toString();
        }
    }

    /**
     * A static, anonymous stub that implements the Benchmark-Interface. Is used to
     * seamlessly handle the case where the BenchmarkEngine is turned off.
     */
    private static final Benchmark BENCHMARK_STUB = new Benchmark() {
        @Override
        public float elapsed() {
            return 0.0f;
        }

        @Override
        public void split(String name) {}

        @Override
        public void end() {}

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public String getName() {
            return "STUB";
        }

        @Override
        public int getRun() {
            return 0;
        }

        @Override
        public String toCSV() {
            return "";
        }
    };

    /** LogManager instance used to log Benchmark results. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** ArrayDeque used to store Benchmark runs if BenchmarkMode == STORE */
    private final ArrayDeque<Benchmark> deque;

    /** */
    private final TObjectIntHashMap<String> counters = new TObjectIntHashMap<>();

    /** Current BenchmarkMode. Cannot be changed once it has been set. */
    private final BenchmarkMode mode;

    /** */
    private final int threshold;

    /**
     * Constructor for BenchmarkEngine.
     *
     * @param mode BenchmarkMode to use.
     */
    public BenchmarkEngine(BenchmarkMode mode, int threshold) {
        this.mode = mode;
        if (mode == BenchmarkMode.STORE) {
            this.deque = new ArrayDeque<>(threshold);
            this.threshold = threshold;
        } else {
            this.deque = null;
            this.threshold = -1;
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public final Benchmark startNew(Class c) {
        return this.startNew(c.getSimpleName());
    }

    /**
     *
     * @param name
     * @return
     */
    public final Benchmark startNew(String name) {
        /* If BenchmarkMode is OFF then return the BENCHMARK_STUB immediately. */
        if (this.mode == BenchmarkMode.OFF) return BENCHMARK_STUB;

        /* Create new Benchmark object and return it */
        int run = this.counters.adjustOrPutValue(name, 1,1);
        Benchmark b = new BenchmarkImpl(name, run);

        /* If storage was configured, then store the Benchmark. */
        if (this.deque != null) {
            synchronized (this) {
                this.deque.add(b);
            }
        }

        /* Return new Benchmark object. */
        return b;
    }

    /**
     * Clears the benchmark and returns
     */
    public synchronized void clear() {
        if (this.deque != null) this.deque.clear();
        this.counters.clear();
    }


    /**
     * Creates and returns a CSV representation of the Benchmark.
     *
     * @return String containing the CSV representation.
     */
    public String toCSV() {
        /* If the deque is null, return empty string. */
        if (this.deque == null) return "";
        StringBuilder builder = new StringBuilder();
        for (Benchmark b : this.deque) {
            builder.append(b.toCSV());
            builder.append("\n");
        }
        return builder.toString();
    }


    /**
     *
     */
    private void flushIfFull() {
        if (this.deque.size() >= this.threshold) {

        }
    }
}
