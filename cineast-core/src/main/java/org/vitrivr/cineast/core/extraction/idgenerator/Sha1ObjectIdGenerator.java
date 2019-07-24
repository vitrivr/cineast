package org.vitrivr.cineast.core.extraction.idgenerator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.LogHelper;

public class Sha1ObjectIdGenerator implements ObjectIdGenerator {

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public String next(Path path, MediaType type) {

    String sha1 = "0000000000000000000000000000000000000000";

    try {
      InputStream is = Files.newInputStream(path);
      sha1 = DigestUtils.sha1Hex(is);
      is.close();
    } catch (IOException e) {
      LOGGER.error("Error while creating SHA1 id for object at '{}': {}", path, LogHelper.getStackTrace(e));
    }

    return MediaType.generateId(type, sha1);

  }
}
