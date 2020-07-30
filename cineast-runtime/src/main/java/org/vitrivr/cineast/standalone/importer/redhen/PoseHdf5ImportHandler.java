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
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.features.pose.PoseKeypoints;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.pose.PoseSpecs;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PoseHdf5ImportHandler extends DataImportHandler {
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

  public PoseHdf5ImportHandler(int threads, int batchsize) {
    super(threads * threadsPerItem(), batchsize);
  }

  private void addFileJobs(String trimmedPath, Path input, List<MediaSegmentDescriptor> objectDescriptors) {
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
  public void doImport(Path root) {
    final DBSelectorSupplier readerSupplier = Config.sharedConfig().getDatabase().getSelectorSupplier();
    if (!readerSupplier.get().ping()) {
      LOGGER.fatal("Database reader unreachable. Aborting...");
      return;
    }
    MediaObjectReader objectReader = new MediaObjectReader(readerSupplier.get());
    MediaSegmentReader segmentReader = new MediaSegmentReader(readerSupplier.get());
    Path rootDir = Files.isDirectory(root) ? root : root.getParent();
    Path mapFilePath = rootDir.resolve("pathMap.json");
    final Map<String, String> pathMap;
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
    try {
      LOGGER.info("Starting import on path {}", root.toAbsolutePath());
      Files.walk(root, 2).filter(
          path -> path.toString().toLowerCase().endsWith(".h5")
      ).forEach(path -> {
        String hdfPath = rootDir.relativize(path).toString();
        String objectPath;
        String trimmedPath = trimExtension(hdfPath);
        if (pathMap != null) {
          objectPath = pathMap.get(hdfPath);
        } else {
          objectPath = trimmedPath + ".mp4";
        }
        System.out.printf("objectPath: %s\n", objectPath);
        final String objectId = objectReader.lookUpObjectByPath(objectPath).getObjectId();
        System.out.printf("objectId: %s\n", objectId);
        List<MediaSegmentDescriptor> objectDescriptors = segmentReader.lookUpSegmentsOfObject(objectId);
        addFileJobs(trimmedPath, path, objectDescriptors);
      });
      LOGGER.info("Waiting for completion");
      this.waitForCompletion();
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
