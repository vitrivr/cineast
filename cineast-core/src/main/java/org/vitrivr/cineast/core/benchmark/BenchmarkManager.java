package org.vitrivr.cineast.core.benchmark;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import org.vitrivr.cineast.core.benchmark.engine.BenchmarkEngine;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkMode;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public final class BenchmarkManager {

    /** Name of the default benchmark-engine. */
    private static String DEFAULT_ENGINE = "benchmark-default";

    /** List of currently running BenchmarkEngines. */
    private static final ConcurrentHashMap<String, BenchmarkEngine> RUNNING = new ConcurrentHashMap<>();

    /**
     * Private constructor; no instantiation.
     */
    private BenchmarkManager() {}

    /**
     * Returns the default BenchmarkEngine configured in the config.json file. Only a single instance of the
     * default BenchmarkEngine exists and it is re-used between calls of this method.
     *
     * @return Instance of the default BenchmarkEngine.
     */
    public static BenchmarkEngine getDefaultEngine() {
        //BenchmarkMode defaultMode = Config.sharedConfig().getBenchmark().getMode();
        //Path defaultPath = Config.sharedConfig().getBenchmark().getPath();
        //TODO: Find better solution
        return getEngine(DEFAULT_ENGINE, BenchmarkMode.STORE, Paths.get("."));
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
    public static BenchmarkEngine getEngine(String name, BenchmarkMode mode, Path path) {
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
    public static void stopEngine(String name) {
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
