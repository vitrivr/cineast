package org.vitrivr.cineast.api.rest.resolvers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;

public class FileSystemThumbnailResolver implements ThumbnailResolver {

  private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
  private final File baseFolder;

  public FileSystemThumbnailResolver(File baseFolder) {
    this.baseFolder = baseFolder;
  }

  @Override
  public ResolutionResult resolve(String segmentId) {

    if (segmentId == null) {
      LOGGER.error("no segment id provided");
      return null;
    }

    String[] split = segmentId.split("_");
    if (split.length < 2) {
      LOGGER.error("unknown segment id format {}", segmentId);
      return null;
    }

    File[] candidates = new File[]{
        new File(baseFolder, segmentId + ".jpg"),
        new File(baseFolder, split[0] + "/" + split[1] + ".jpg")
    };

    // If there are 3 or more elements in the split array, include appropriate candidates
    if (split.length >= 3) {
      File[] extraCandidates = new File[]{
          new File(baseFolder, split[0] + "_" + split[1] + "/" + split[2] + ".jpg"),
          new File(baseFolder, split[0] + "_" + split[1] + "/" + segmentId + ".jpg"),
          new File(baseFolder, split[0] + "_" + String.join("_", Arrays.copyOfRange(split, 1, split.length - 1)) + "/" + segmentId + ".jpg"),
          new File(baseFolder, split[0] + "_" + String.join("_", Arrays.copyOfRange(split, 1, split.length - 1)) + "/" + segmentId + ".png"),
          new File(baseFolder, split[0] + "_" + split[1] + "/" + split[2] + ".png"),
          new File(baseFolder, split[0] + "_" + split[1] + "/" + segmentId + ".png"),
          new File(baseFolder, split[1] + "/" + split[2] + ".jpg"),
          new File(baseFolder, split[1] + "/" + split[2] + ".png"),
          new File(baseFolder, split[1] + "/" + split[1] + "_" + split[2] + ".jpg"),
          new File(baseFolder, split[1] + "/" + split[1] + "_" + split[2] + ".png"),
          new File(baseFolder, split[1] + "/shot" + split[1] + "_" + split[2] + ".jpg"),
          new File(baseFolder, split[1] + "/shot" + split[1] + "_" + split[2] + ".png")
      };
      candidates = Arrays.copyOf(candidates, candidates.length + extraCandidates.length);
      System.arraycopy(extraCandidates, 0, candidates, candidates.length - extraCandidates.length, extraCandidates.length);
    }

    for (File candidate : candidates) {
      if (candidate.exists() && candidate.canRead()) {
        try {
          return new ResolutionResult(candidate);
        } catch (FileNotFoundException e) {
          LOGGER.error(e);
          return null;
        }
      }
    }
    LOGGER.error("no thumbnail found for segment id {}", segmentId);
    return null;
  }
}
