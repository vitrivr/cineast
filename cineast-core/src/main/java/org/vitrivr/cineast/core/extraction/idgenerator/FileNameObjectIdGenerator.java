package org.vitrivr.cineast.core.extraction.idgenerator;

import org.apache.commons.io.FilenameUtils;
import org.vitrivr.cineast.core.data.MediaType;

import java.nio.file.Path;
import java.util.Map;

/**
 * Generates object IDs from object file names (without file extension).
 * <p>
 * Includes type prefix if "prefix" property is not set to false.
 */
public class FileNameObjectIdGenerator implements ObjectIdGenerator {

  /**
   * Prefix property name. Can be set to false to prevent type prefix in object ID.
   */
  private static final String PROPERTY_NAME_PREFIX = "prefix";

  private final boolean prefix;

  public FileNameObjectIdGenerator() {
    prefix = true;
  }

  public FileNameObjectIdGenerator(Map<String, String> properties) {
    String prefixProp = properties.get(PROPERTY_NAME_PREFIX);
    prefix = prefixProp == null || prefixProp.equalsIgnoreCase("true");
  }

  @Override
  public String next(Path path, MediaType type) {

    if (path == null) {
      return "null";
    }

    String filename = FilenameUtils.removeExtension(path.toFile().getName());
    return prefix ? MediaType.generateId(type, filename) : filename;
  }
}
