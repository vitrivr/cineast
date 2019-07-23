package org.vitrivr.cineast.core.benchmark.engine;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.benchmark.model.Benchmark;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkImpl;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkMode;

import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public class BenchmarkEngine {
    /**
     * Inner class extension of the BenchmarkImpl.
     */
    private class InnerBenchmark extends BenchmarkImpl {

        /**
         * Default constr
         * @param name
         */
        private InnerBenchmark(String name) {
            this(name, 1);
        }

        /**
         *
         * @param name
         * @param run
         */
        private InnerBenchmark(String name, int run) {
            super(name, run);
            BenchmarkEngine.LOGGER.debug("Beginning Benchmark for {}.", this.name);
        }

        /**
         * Completes the Benchmark and sets the end-timestamp. This marks the regular
         * end of the Benchmark.
         */
        @Override
        public void end() {
            super.end();
            BenchmarkEngine.LOGGER.debug("Ended Benchmark for {} (Elapsed: {}s).", this.name, this.elapsed());
        }

        /**
         * Aborts the Benchmark. This marks an irregular end of the Benchmark,
         * e.g. due to an error.
         *
         * An aborted Benchmark will not be stored!
         */
        @Override
        public void abort() {
            super.abort();
            BenchmarkEngine.this.deque.remove(this);
            BenchmarkEngine.LOGGER.debug("Aborted Benchmark for {} (Elapsed: {}s).", this.name, this.elapsed());
        }

        /**
         * Registers a named split. A name can only be used once.
         *
         * @param name Name of the split.
         */
        @Override
        public void split(String name) {
            super.split(name);
            BenchmarkEngine.LOGGER.debug("Added split '{}' for benchmark '{}' (Elapsed: {}s).", name, this.name, this.elapsed());
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
        public void abort() {}

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
        public Map<String, Float> splitDurations() {
            return new LinkedHashMap<>(0);
        }

        @Override
        public Map<String,Object> data() {
            return new HashMap<>(0);
        }
    };

    /** LogManager instance used to log Benchmark results. */
    private static final Logger LOGGER = LogManager.getLogger();

    /** ArrayDeque used to store Benchmark runs if BenchmarkMode == STORE */
    private final ArrayDeque<Benchmark> deque;

    /** Map that keeps track of the runs that were started per category. */
    private final TObjectIntHashMap<String> counters = new TObjectIntHashMap<>();

    /** Current BenchmarkMode. Cannot be changed once it has been set. */
    private final BenchmarkMode mode;

    /** Name of the benchmark-engine. */
    private final String name;

    /** The thread that runs the BenchmarkWriter class (class responsible for writing BenchmarkEntries to disk). */
    private final Thread writerThread;

    /** Flag indicating, that the BenchmarkEngine was stopped. Stopped BenchmarkEngines cannot start new Benchmarks anymore. */
    private volatile boolean stopped = false;

    /**
     * Constructor for BenchmarkEngine.
     *
     * @param mode BenchmarkMode to use.
     */
    public BenchmarkEngine(String name, BenchmarkMode mode, Path path) {
        this.mode = mode;
        this.name = name;
        if (mode == BenchmarkMode.STORE) {
            this.deque = new ArrayDeque<>();
            this.writerThread = new Thread(new BenchmarkWriter(this, path));
            this.writerThread.setPriority(Thread.MIN_PRIORITY);
            this.writerThread.setName("BenchmarkWriter (" + name + ")");
            this.writerThread.setDaemon(true);
            this.writerThread.start();
        } else {
            this.deque = null;
            this.writerThread = null;
        }
    }

    /**
     * Getter for the BenchmarkEngine's name.
     *
     * @return Name of the BenchmarkEngine.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Starts a new benchmark and returns it. The returned Benchmark object can be used in the benchmarked code
     * to register splits and end the benchmark.
     *
     * @param c Class that is being benchmarked. Is going to serve as name.
     * @return Benchmark object.
     */
    public final Benchmark startNew(Class<?> c) {
        return this.startNew(c.getSimpleName());
    }

    /**
     * Starts a new benchmark and returns it. The returned Benchmark object can be used within the benchmarked code
     * to register splits and end the benchmark.
     *
     * @param name Name of the benchmark.
     * @return Benchmark object.
     */
    public final Benchmark startNew(String name) {
        /* If BenchmarkMode is OFF then return the BENCHMARK_STUB immediately. */
        if (this.mode == BenchmarkMode.OFF) {
          return BENCHMARK_STUB;
        }

        /* If BenchmarkEngine was stopped, return null. */
        if (this.stopped) {
          return null;
        }

        /* Create new Benchmark object and return it */
        int run = this.counters.adjustOrPutValue(name, 1,1);
        Benchmark b = new InnerBenchmark(name, run);

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
     * Stops the BenchmarkEngine which means that no more new Benchmarks can be created from now on.
     * Benchmarks that are still running, can still be used and completed.
     *
     * Calling this method will also cause the underlying BenchmarkWriter to stop its work. Hence,
     * Benchmarks that are still running will not be written to disk.
     */
    public void stop() {
        if (this.writerThread != null) {
            this.writerThread.interrupt();
        }
        this.stopped = true;
    }

    /**
     * Clears the benchmark's dequeue.
     */
    public synchronized void clear() {
        if (this.deque != null) {
          this.deque.clear();
        }
        this.counters.clear();
    }


    /**
     * Drains all the Benchmark-objects in the Dequeue and returns them in a List. If a Benchmark
     * is still running, that object is returned to the deque and draining in ceased. Invocation of
     * this method does not affect the counter-object.
     *
     * @return List of drained Benchmark objects.
     */
    public synchronized List<Benchmark> drain() {
        /* If the deque is null, return empty string. */
        if (this.deque == null || this.deque.isEmpty()) {
          return new ArrayList<>(1);
        }

        /* Remove elements from Dequeue, until it's empty or the returned element is still running. */
        List<Benchmark> benchmarks = new ArrayList<>(this.deque.size());
        Benchmark benchmark = null;
        while ((benchmark = this.deque.pollLast()) != null) {
            if (!benchmark.isRunning()) {
                benchmarks.add(benchmark);
            } else {
                this.deque.offerLast(benchmark);
                break;
            }
        }
        return benchmarks;
    }
}
