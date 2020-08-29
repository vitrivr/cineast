package org.vitrivr.cineast.standalone.importer.redhen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.features.pose.PoseKeypoints;
import org.vitrivr.cineast.core.util.pose.PoseSpecs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PoseHdf5ImportHandler extends AbstractHdf5ImporterHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  protected Pattern[] getH5Exts() {
    return new Pattern[]{
        Pattern.compile("\\.h5$"),
        Pattern.compile("\\.(un)?(sorted|seg)$"),
        Pattern.compile("_openpose$")
    };
  }

  private static int threadsPerItem() {
    // Generator thread + 1 importer thread per spec
    return 1 + PoseSpecs.getInstance().specs.size();
  }

  public PoseHdf5ImportHandler(int threads, int batchsize) {
    super(threads * threadsPerItem(), batchsize);
  }

  @Override
  protected void addFileJobs(String fullPath, Path input) {
    String trimmedPath = trimExtension(fullPath);
    String objectPath = objectPathOfFilesystemPath(fullPath);
    List<MediaSegmentDescriptor> objectDescriptors = getObjectDescriptorsByPath(objectPath);
    PoseHdf5Generator poseHdf5Generator = new PoseHdf5Generator(input, objectDescriptors);
    this.futures.add(this.service.submit(poseHdf5Generator));
    for (String poseSpecName : PoseSpecs.getInstance().specs.keySet()) {
      this.futures.add(this.service.submit(new DataImportRunner(
          new PoseHdf5Importer(poseHdf5Generator, poseSpecName),
          PoseKeypoints.POSE_KEYPOINTS_TABLE_START + poseSpecName,
          "pose-keypoints-file-" + trimmedPath + "-" + poseSpecName
      )));
    }
  }
}
