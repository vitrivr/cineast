package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;

public class FileSystemObjectResolver implements ObjectResolver {

  private final MultimediaObjectLookup lookup;
  private final File baseDir;

  public FileSystemObjectResolver(File basedir, MultimediaObjectLookup lookup){
    this.lookup = lookup;
    this.baseDir = basedir;
  }

  public FileSystemObjectResolver(File basedir){
    this(basedir, new MultimediaObjectLookup());
  }

  @Override
  public ResolutionResult resolve(String id) {

    if(id == null){
      return null;
    }

    MultimediaObjectDescriptor descriptor = this.lookup.lookUpObjectById(id);


    if(!descriptor.exists()){
      return null;
    }

    try{
      return new ResolutionResult(
          "video/mp4", //TODO determine mime type
          new FileInputStream(
              new File(baseDir, descriptor.getPath())
          )
      );
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
