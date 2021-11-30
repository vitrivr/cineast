package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.IOException;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;

public class FileSystemObjectResolver implements ObjectResolver, AutoCloseable {

  private final MediaObjectReader lookup;
  private final File baseDir;

  private final ObjectToFileResolver object2File;

  public FileSystemObjectResolver(File basedir, MediaObjectReader lookup) {
    this.lookup = lookup;
    this.baseDir = basedir;
    object2File = (dir, obj) -> new File(dir, obj.getPath());
  }

  public FileSystemObjectResolver(File basedir, MediaObjectReader lookup, ObjectToFileResolver transform) {
    this.lookup = lookup;
    this.baseDir = basedir;
    object2File = transform;
  }


  @Override
  public ResolutionResult resolve(String id) {

    if (id == null) {
      return null;
    }

    MediaObjectDescriptor descriptor = this.lookup.lookUpObjectById(id);

    if (!descriptor.exists()) {
      return null;
    }

    try {
      return new ResolutionResult(object2File.resolve(baseDir, descriptor));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void close() throws Exception {
    this.lookup.close();
  }
}
