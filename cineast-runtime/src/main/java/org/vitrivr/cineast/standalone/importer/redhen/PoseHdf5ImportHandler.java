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

public class PoseHdf5ImportHandler extends AligningImportHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  private static final Pattern[] H5_EXT = {
      Pattern.compile("\\.h5$"),
      Pattern.compile("\\.(un)?(sorted|seg)$"),
      Pattern.compile("_openpose$")
  };

  static private String trimExtension(String path) {
    for (Pattern pat : H5_EXT) {
      path = pat.matcher(path).replaceAll("");
    }
    return path;
  }

  private static int threadsPerItem() {
    // Generator thread + 1 importer thread per spec
    return 1 + PoseSpecs.getInstance().specs.size();
  }

  private Map<String, String> pathMap;

  public PoseHdf5ImportHandler(int threads, int batchsize) {
    super(threads * threadsPerItem(), batchsize);
  }

  @Override
  protected void addFileJobs(String fullPath, Path input) {
    String trimmedPath = trimExtension(fullPath);
    String objectPath = objectPathOfFilesystemPath(fullPath);
    List<MediaSegmentDescriptor> objectDescriptors = getObjectDescriptors(objectPath);
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

  @Override
  protected String getExtension() {
    return ".h5";
  }

  private String objectPathOfFilesystemPath(String fsPath) {
    if (pathMap != null) {
      return pathMap.get(fsPath);
    } else {
      return trimExtension(fsPath) + ".mp4";
    }
  }

  @Override
  public void doImport(Path root) {
    Path mapFilePath = root.resolve("pathMap.json");
    if (Files.exists(mapFilePath)) {
      ObjectMapper jsonMapper = new ObjectMapper();
      TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
      try {
        pathMap = jsonMapper.readValue(mapFilePath.toFile(), typeRef);
      } catch (IOException e) {
        LOGGER.fatal("Could not read map file {}", mapFilePath.toString());
        throw new RuntimeException("Could not read map file");
      }
    } else {
      pathMap = null;
    }
    super.doImport(root);
  }
}
