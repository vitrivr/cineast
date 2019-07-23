package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.IOException;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;

public class FileSystemObjectResolver implements ObjectResolver {

  private final MediaObjectReader lookup;
  private final File baseDir;

  public FileSystemObjectResolver(File basedir, MediaObjectReader lookup){
    this.lookup = lookup;
    this.baseDir = basedir;
  }

  public FileSystemObjectResolver(File basedir){
    this(basedir, new MediaObjectReader());
  }

  @Override
  public ResolutionResult resolve(String id) {

    if(id == null){
      return null;
    }

    MediaObjectDescriptor descriptor = this.lookup.lookUpObjectById(id);


    if(!descriptor.exists()){
      return null;
    }

    try{
      return new ResolutionResult(new File(baseDir, descriptor.getPath()));
    }catch (IOException e){
      e.printStackTrace();
      return null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    this.lookup.close();
    super.finalize();
  }
}
