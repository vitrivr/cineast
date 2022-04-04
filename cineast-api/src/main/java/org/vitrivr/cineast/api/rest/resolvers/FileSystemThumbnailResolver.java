package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.FileNotFoundException;

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

    File[] candidates = new File[]{
      new File(baseFolder, split[0] + "_" + split[1] + "/" + split[2] + ".jpg"),
      new File(baseFolder, split[0] + "_" + split[1] + "/" + split[2] + ".png"),
      new File(baseFolder, split[1] + "/" + split[2] + ".jpg"),
      new File(baseFolder, split[1] + "/" + split[2] + ".png"),
      new File(baseFolder, split[1] + "/" + split[1] + "_" + split[2] + ".jpg"),
      new File(baseFolder, split[1] + "/" + split[1] + "_" + split[2] + ".png"),
      new File(baseFolder, split[1] + "/shot" + split[1] + "_" + split[2] + ".jpg"),
      new File(baseFolder, split[1] + "/shot" + split[1] + "_" + split[2] + ".png"),
    };

    for (File candidate : candidates) {
      if (candidate.exists() && candidate.canRead()) {
        try {
          return new ResolutionResult(candidate);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          return null;
        }
      }
    }

    return null;
  }
}
