package org.vitrivr.cineast.core.benchmark;

import org.vitrivr.cineast.core.benchmark.engine.BenchmarkEngine;
import org.vitrivr.cineast.core.benchmark.model.Benchmark;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkImpl;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkMode;
import org.vitrivr.cineast.core.config.BenchmarkConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;


public final class BenchmarkManager {

    private static BenchmarkManager INSTANCE;

    public static synchronized BenchmarkManager getInstance(){ //FIXME this has no chance to use the actual config file
        if (INSTANCE == null){
            INSTANCE = new BenchmarkManager(new BenchmarkConfig());
        }
        return INSTANCE;
    }

    /** Name of the default benchmark-engine. */
    private static final String DEFAULT_ENGINE = "benchmark-default";

    /** List of currently running BenchmarkEngines. */
    private final ConcurrentHashMap<String, BenchmarkEngine> RUNNING = new ConcurrentHashMap<>();

    private final BenchmarkConfig config;

    public BenchmarkManager(BenchmarkConfig benchmarkConfig) {
        this.config = benchmarkConfig;
    }

    /**
     * Returns the default BenchmarkEngine configured in the config.json file. Only a single instance of the
     * default BenchmarkEngine exists and it is re-used between calls of this method.
     *
     * @return Instance of the default BenchmarkEngine.
     */
    public BenchmarkEngine getDefaultEngine() {
        return getEngine(DEFAULT_ENGINE, config.getMode(), config.getPath());
    }

    /**
     * Returns a new BenchmarkEngine that uses the provided name and configuration. If a BenchmarkEngine for
     * that name was instantiated before, that engine is re-used between calls to this methods.
     *
     * @param name Name of the BenchmarkEngine.
     * @param mode BenchmarkMode to use with the new engine.
     * @param path Path where the benchmark-engine should write its output. Has no effect for every mode except BenchmarkMode.STORE
     * @return Instance of named BenchmarkEngine
     */
    public BenchmarkEngine getEngine(String name, BenchmarkMode mode, Path path) {
        synchronized (RUNNING) {
            if (!RUNNING.containsKey(name)) {
              RUNNING.put(name, new BenchmarkEngine(name, mode, path));
            }
        }
        return RUNNING.get(name);
    }

    /**
     * Stops a named BenchmarkEngine and removes it from the list of running BenchmarkEngines.
     *
     * @param name Name of the BenchmarkEngine to stop
     */
    public void stopEngine(String name) {
        if (name.equals(DEFAULT_ENGINE)) {
          throw new IllegalArgumentException("You cannot stop the default benchmark engine.");
        }
        synchronized(RUNNING){
            if (RUNNING.containsKey(name)) {
                RUNNING.get(name).stop();
                RUNNING.remove(name);
            }
        }
    }
}
