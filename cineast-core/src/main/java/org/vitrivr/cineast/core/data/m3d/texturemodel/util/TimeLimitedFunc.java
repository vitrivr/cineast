package org.vitrivr.cineast.core.data.m3d.texturemodel.util;

import java.util.concurrent.*;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.assimp.Assimp.aiImportFile;

public class  TimeLimitedFunc <T> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final long timeLimit;
    private final Supplier<T> sup;

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    T value = null;


    public TimeLimitedFunc(long timeLimit, Supplier<T> sup) {
        this.timeLimit = timeLimit;
        this.sup = sup;
    }

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
            throw new TimeoutException("Timeout");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.value;
    }
}

