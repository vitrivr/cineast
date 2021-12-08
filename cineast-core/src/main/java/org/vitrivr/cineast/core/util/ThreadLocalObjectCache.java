package org.vitrivr.cineast.core.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ThreadLocalObjectCache<T> {

    private final LoadingCache<Thread, T> cache;

    public ThreadLocalObjectCache(CacheLoader<Thread, T> cacheLoader) {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build(cacheLoader);
    }

    public T get() {
        try {
            return this.cache.get(Thread.currentThread());
        } catch (ExecutionException executionException) {
            throw new IllegalStateException("Error accessing ThreadLocalObjectCache for thread " + Thread.currentThread().getName());
        }
    }

}
