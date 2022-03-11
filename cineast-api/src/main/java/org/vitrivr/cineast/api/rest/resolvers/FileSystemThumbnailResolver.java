package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class FileSystemThumbnailResolver implements ThumbnailResolver {

  private final File baseFolder;

  public FileSystemThumbnailResolver(File baseFolder) {
    this.baseFolder = baseFolder;
  }

  @Override
  public ResolutionResult resolve(String segmentId) {

    if (segmentId == null) {
      return null;
    }

    String[] split = segmentId.split("_");
    if (split.length < 3) {
      return null;
    }

    //String fileName = segmentId.substring(0, segmentId.lastIndexOf("_"));

    File dir = new File(this.baseFolder, split[1]);

    if (!dir.exists() || !dir.isDirectory()) {
      return null;
    }

    File[] candidates = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("shot" + split[1] + "_" + split[2]);
      }
    });

    if (candidates.length == 0) {
      return null;
    }

    //TODO prioritize file endings

    try {
      return new ResolutionResult(candidates[0]);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
}
