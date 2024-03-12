package org.vitrivr.cineast.core.data.m3d.texturemodel.util;

import java.util.concurrent.*;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.assimp.Assimp.aiImportFile;

public class  TimeLimitedFunc <T> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Time limit in seconds */
    private final long timeLimit;

    /** Supplier of T, which is the function to be executed and timed */
    private final Supplier<T> sup;

    /** Executor service to run the task */
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     *  Return value
     */
    T value = null;


    /**
     * Constructor for TimeLimitedFunc
     * @param timeLimit time limit in seconds
     * @param sup Supplier of T
     */
    public TimeLimitedFunc(long timeLimit, Supplier<T> sup) {
        this.timeLimit = timeLimit;
        this.sup = sup;
    }

    /**
     * Run the function with a time limit
     * @return T
     * @throws TimeoutException if the function takes longer than the time limit
     */
    public T runWithTimeout() throws TimeoutException {
        Runnable task = () -> {
            this.value = sup.get();
        };
        Future<?> future = executor.submit(task);
        try {
            future.get(this.timeLimit, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            LOGGER.error("Error Timeout ", e);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.value;
    }
}

