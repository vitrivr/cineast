package org.vitrivr.cineast.core.benchmark;

import org.vitrivr.cineast.core.benchmark.engine.BenchmarkEngine;
import org.vitrivr.cineast.core.benchmark.model.BenchmarkMode;
import org.vitrivr.cineast.core.config.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public final class BenchmarkManager {

    /** */
    private static String DEFAULT_ENGINE = "DEFAULT";

    /** */
    private static final HashMap<String,BenchmarkEngine> RUNNING = new HashMap<>();

    /**
     * Private constructor; no instantiation.
     */
    private BenchmarkManager() {}

    /**
     *
     * @return
     */
    public static BenchmarkEngine getDefaultEngine() {
        BenchmarkMode defaultMode = Config.sharedConfig().getBenchmark().getMode();
        Path defaultPath = Paths.get(Config.sharedConfig().getBenchmark().getPath());
        return getEngine(DEFAULT_ENGINE, defaultMode, defaultPath);
    }

    /**
     *
     * @param name
     * @param mode
     * @param path
     * @return
     */
    public static BenchmarkEngine getEngine(String name, BenchmarkMode mode, Path path) {
        synchronized (RUNNING) {
            if (!RUNNING.containsKey(name)) RUNNING.put(name, new BenchmarkEngine(name, mode, path));
        }
        return RUNNING.get(name);
    }

    /**
     *
     * @param name
     * @return
     */
    public static void stopEngine(String name) {
        if (name.equals(DEFAULT_ENGINE)) throw new IllegalArgumentException("You cannot stop the default benchmark engine.");
        synchronized(RUNNING){
            if (RUNNING.containsKey(name)) {
                RUNNING.remove(name);
            }
        }
    }
}
