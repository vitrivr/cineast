package org.vitrivr.cineast.api.rest.resolvers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;

public class FileSystemObjectResolver implements ObjectResolver, AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();
  private final MediaObjectReader lookup;
  private final File baseDir;
  private final LoadingCache<String, MediaObjectDescriptor> descriptorLoadingCache;
  private final ObjectToFileResolver object2File;

  public FileSystemObjectResolver(File basedir, MediaObjectReader lookup) {
    this(basedir, lookup, (dir, obj) -> new File(dir, obj.getPath()));
  }

  public FileSystemObjectResolver(File basedir, MediaObjectReader lookup, ObjectToFileResolver transform) {
    this.lookup = lookup;
    this.baseDir = basedir;
    this.object2File = transform;

    this.descriptorLoadingCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<>() {
      @Override
      public MediaObjectDescriptor load(String key) {
        return lookup.lookUpObjectById(key);
      }
    });
  }


  @Override
  public ResolutionResult resolve(String id) {

    if (id == null) {
      return null;
    }

    MediaObjectDescriptor descriptor;
    try {
      descriptor = descriptorLoadingCache.get(id);
    } catch (ExecutionException e) {
      LOGGER.error(e);
      return null;
    }

    if (!descriptor.exists()) {
      return null;
    }

    try {
      return new ResolutionResult(object2File.resolve(baseDir, descriptor));
    } catch (IOException e) {
      LOGGER.error(e);
      return null;
    }
  }

  @Override
  public void close() {
    this.lookup.close();
  }
}
