package org.vitrivr.cineast.core.data.raw;


/**
 * A cacheable piece of data as created by a {@link CachedDataFactory}.
 *
 * @author Ralph Gasser
 * @version 1.0
 * @see CachedDataFactory
 */
public interface CacheableData {
    /**
     * Exposes the {@link CachedDataFactory} that created this instance of {@link CacheableData}.
     *
     * @return {@link CachedDataFactory} reference.
     */
    CachedDataFactory factory();
}
