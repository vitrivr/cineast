package org.vitrivr.cineast.standalone.importer.redhen;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.features.pose.HandPoseEmbedding;

public class HandPreEmbedImportHandler extends AbstractHdf5ImporterHandler {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final Pattern LEFT_HAND_PAT = Pattern.compile("\\.lefthand");
  private static final Pattern[] HDF_EXTS = {
    Pattern.compile("\\.h5$"),
    Pattern.compile("\\.json$"),
    Pattern.compile("\\.preembed$"),
    Pattern.compile("\\.lefthand$"),
    Pattern.compile("\\.righthand$")
  };
  private final boolean useJson;

  protected Pattern[] getH5Exts() {
    return HDF_EXTS;
  }

  @Override
  protected String getExtension() {
    if (useJson) {
      return ".json";
    } else {
      return ".h5";
    }
  }

  public HandPreEmbedImportHandler(int threads, int batchsize, boolean useJson) {
    super(threads, batchsize);
    this.useJson = useJson;
  }

  @Override
  protected void addFileJobs(String fullPath, Path input) {
    String trimmedPath = trimExtension(fullPath);
    String objectPath = objectPathOfFilesystemPath(fullPath);
    boolean isLeft = LEFT_HAND_PAT.matcher(fullPath).find();
    String poseSpecName;
    if (isLeft) {
      poseSpecName = "LEFT_HAND_IN_BODY_25";
    } else {
      poseSpecName = "RIGHT_HAND_IN_BODY_25";
    }
    LOGGER.info("Importing {} of {} from {}", poseSpecName, objectPath, fullPath);
    List<MediaSegmentDescriptor> objectDescriptors = getObjectDescriptorsByPath(objectPath);
    this.futures.add(this.service.submit(new DataImportRunner(
        new HandPreEmbedImporter(input, objectDescriptors, this.useJson),
        HandPoseEmbedding.HAND_POSE_EMBEDDING_TABLE_START + poseSpecName,
        "pose-keypoints-file-" + trimmedPath + "-" + poseSpecName
    )));
  }
}
