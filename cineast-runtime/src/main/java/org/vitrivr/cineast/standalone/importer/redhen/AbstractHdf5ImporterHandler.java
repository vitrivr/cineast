package org.vitrivr.cineast.standalone.importer.redhen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class AbstractHdf5ImporterHandler extends AligningImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();
  public AbstractHdf5ImporterHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  abstract protected Pattern[] getH5Exts();
  private Map<String, String> pathMap;

  protected String trimExtension(String path) {
    for (Pattern pat : getH5Exts()) {
      path = pat.matcher(path).replaceAll("");
    }
    return path;
  }

  @Override
  protected String getExtension() {
    return ".h5";
  }

  protected String objectPathOfFilesystemPath(String fsPath) {
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
