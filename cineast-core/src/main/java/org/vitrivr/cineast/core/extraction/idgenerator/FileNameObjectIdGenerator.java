package org.vitrivr.cineast.core.extraction.idgenerator;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.vitrivr.cineast.core.data.MediaType;

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
    String prefixProp = properties.getOrDefault(PROPERTY_NAME_PREFIX, "true");
    prefix = prefixProp.equalsIgnoreCase("true");
  }

  @Override
  public Optional<String> next(Path path, MediaType type) {

    if (path == null) {
      return Optional.empty();
    }

    String filename = FilenameUtils.removeExtension(path.toFile().getName());
    String id = prefix ? MediaType.generateId(type, filename) : filename;
    return Optional.of(id);
  }
}
