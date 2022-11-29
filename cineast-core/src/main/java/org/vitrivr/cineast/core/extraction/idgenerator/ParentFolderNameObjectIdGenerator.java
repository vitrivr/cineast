package org.vitrivr.cineast.core.extraction.idgenerator;

import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.vitrivr.cineast.core.data.MediaType;

/**
 * Generates object IDs from the containing parent directory of the object file.
 * <p>
 * Includes type prefix if "prefix" property is set to true.
 */
public class ParentFolderNameObjectIdGenerator implements ObjectIdGenerator {

  /**
   * Prefix property name. Can be set to false to prevent type prefix in object ID.
   */
  private static final String PROPERTY_NAME_PREFIX = "prefix";

  private final boolean prefix;

  public ParentFolderNameObjectIdGenerator() {
    prefix = true;
  }

  public ParentFolderNameObjectIdGenerator(Map<String, String> properties) {
    String prefixProp = properties.get(PROPERTY_NAME_PREFIX);
    prefix = prefixProp == null || prefixProp.equalsIgnoreCase("true");
  }

  @Override
  public String next(Path path, MediaType type) {

    if (path == null) {
      return "null";
    }

    String filename = FilenameUtils.getBaseName(path.toFile().getParentFile().getName());
    return prefix ? MediaType.generateId(type, filename) : filename;
  }
}
