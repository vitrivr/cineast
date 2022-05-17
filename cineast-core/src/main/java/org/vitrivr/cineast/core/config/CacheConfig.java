package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

public final class CacheConfig {

  private static final Logger LOGGER = LogManager.getLogger();
  private final UUID uuid = UUID.randomUUID();
  private Policy cachingPolicy = Policy.AUTOMATIC;
  private Path cacheLocation = Paths.get(".");
  private CachedDataFactory factory;

  private boolean enableQueryCaching = false;
  /** maximum number of queries / ids to cache results for */
  private int queryCacheSize = 100;
  /** maximum duration in seconds for which to cache a result */
  private long queryCacheDuration = 600;

  public CacheConfig() {
  }



  /**
   * @param cachePolicy   Caching Policy
   * @param cacheLocation the file system location of the disk cache
   * @throws IllegalArgumentException in case any of the memory limits is negative
   * @throws NullPointerException     in case the cachePolicy or cacheLocation is null
   * @throws SecurityException        in case access to cacheLocation is not permitted
   */
  @JsonCreator
  public CacheConfig(
      @JsonProperty(value = "cachePolicy", required = false, defaultValue = "AUTOMATIC") String cachePolicy,
      @JsonProperty(value = "cacheLocation", required = false, defaultValue = ".") String cacheLocation) {

    if (cachePolicy == null) {
      cachePolicy = "AUTOMATIC";
    }
    if (cacheLocation == null) {
      cacheLocation = ".";
    }

    final Path location = Paths.get(cacheLocation);
    if (!Files.exists(location)) {
      try {
        Files.createDirectories(location);
        this.cacheLocation = location;
      } catch (IOException e) {
        this.cacheLocation = Paths.get(".");
        LOGGER.warn("Specified cache location ({}) could not be created! Fallback to default location: {}", location.toAbsolutePath().toString(), this.cacheLocation.toAbsolutePath().toString());
      }
    } else if (!Files.isDirectory(location)) {
      this.cacheLocation = Paths.get(".");
      LOGGER.warn("Specified cache location ({}) could not be created! Fallback to default location: {}", location.toAbsolutePath().toString(), this.cacheLocation.toAbsolutePath().toString());
    } else {
      this.cacheLocation = location;
    }
    this.cachingPolicy = Policy.valueOf(cachePolicy);
  }

  /**
   * @return the caching policy
   */
  @JsonProperty
  public final Policy getCachingPolicy() {
    return this.cachingPolicy;
  }

  public void setCachingPolicy(Policy cachingPolicy) {
    if (cachingPolicy == null) {
      throw new NullPointerException("CachePolicy cannot be null");
    }
    this.cachingPolicy = cachingPolicy;
  }

  /**
   * @return the file system location of the cache
   */
  @JsonProperty
  public final Path getCacheLocation() {
    return this.cacheLocation;
  }

  public void setCacheLocation(Path cacheLocation) {
    if (cacheLocation == null) {
      throw new NullPointerException("CacheLocation cannot be null");
    }
    this.cacheLocation = cacheLocation;
  }

  /**
   * Returns the UUID of this {@link CacheConfig}.
   *
   * @return UUID of this {@link CacheConfig}.
   */
  public String getUUID() {
    return this.uuid.toString();
  }

  /**
   * A simple heuristic to determine whether an object of the given size should be cached or kept in-memory.
   *
   * @param size Size of the object in bytes.
   * @return True if object should be kept in memory, false otherwise.
   */
  public boolean keepInMemory(int size) {
    final double utilisation = ((double) (Runtime.getRuntime().freeMemory() + size + 24) / (double) Runtime.getRuntime().maxMemory());
    switch (this.cachingPolicy) {
      case DISK_CACHE:
        return false;
      case AUTOMATIC:
        return utilisation > 0.65; /* If more than 65% of memory is occupied, we start caching. */
      case AVOID_CACHE:
        return size > 0.90; /* If more than 90% of memory is occupied, we start caching. */
      default:
        return true;
    }
  }

  /**
   * Returns and optionally creates the shared {@link CachedDataFactory} instance created by this {@link CacheConfig}.
   *
   * @return Shared {@link CachedDataFactory}.
   */
  public synchronized CachedDataFactory sharedCachedDataFactory() {
    if (this.factory == null) {
      this.factory = new CachedDataFactory(this);
    }
    return this.factory;
  }

  public boolean isEnableQueryCaching() {
    return enableQueryCaching;
  }

  public void setEnableQueryCaching(boolean enableQueryCaching) {
    this.enableQueryCaching = enableQueryCaching;
  }

  public int getQueryCacheSize() {
    return queryCacheSize;
  }

  /**
   * negative value will disable cache
   */
  public void setQueryCacheSize(int queryCacheSize) {
    if (queryCacheSize < 0) {
      this.queryCacheSize = 0;
      this.enableQueryCaching = false;
    } else {
      this.queryCacheSize = queryCacheSize;
    }
  }

  public long getQueryCacheDuration() {
    return queryCacheDuration;
  }

  /**
   * negative value will disable cache
   */
  public void setQueryCacheDuration(long queryCacheDuration) {
    if (queryCacheDuration < 0) {
      this.queryCacheDuration = 0;
      this.enableQueryCaching = false;
    } else {
      this.queryCacheDuration = queryCacheDuration;
    }
  }

  @Override
  public String toString() {
    return "\"cache\" : { \"cachePolicy\" : \"" + this.cachingPolicy.toString() + ", \"cacheLocation\" : \"" + this.cacheLocation.toString() + "\" }";
  }

  public enum Policy {
    DISK_CACHE, //cache to disk unless newInMemoryMultiImage is requested
    AUTOMATIC, //keep in memory as long as threshold is not exceeded, makes exceptions for images not generated by the video decoder
    AVOID_CACHE //avoids cache until hard limit is reached
  }
}
