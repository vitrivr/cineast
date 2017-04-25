package org.vitrivr.cineast.core.benchmark;

import org.vitrivr.cineast.core.config.Config;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public final class BenchmarkManager {

    /** */
    private static BenchmarkEngine DEFAULT_ENGINE;

    /**
     * Private constructor; no instantiation.
     */
    private BenchmarkManager() {}

    /**
     *
     * @return
     */
    public static BenchmarkEngine getDefaultEngine() {
        if (DEFAULT_ENGINE == null) {
            DEFAULT_ENGINE = new BenchmarkEngine(Config.sharedConfig().getBenchmark().getMode(),10);
        }
        return DEFAULT_ENGINE;
    }

    /**
     *
     * @param mode
     * @return
     */
    public static BenchmarkEngine getEngine(BenchmarkMode mode) {
        return new BenchmarkEngine(mode, 10);
    }

}
