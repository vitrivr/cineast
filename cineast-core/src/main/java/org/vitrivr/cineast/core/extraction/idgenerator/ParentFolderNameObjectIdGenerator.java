package org.vitrivr.cineast.core.extraction.idgenerator;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.vitrivr.cineast.core.data.MediaType;

/**
 * Generates object IDs from the containing parent directory of the object file.
 * <p>
 * The prerequisite is that there is only one media type in the folder at a time that is to be extracted.
 * Includes type prefix if "prefix" property is set to true.
 * For example, this ObjectIdGenerator is suitable for 3D models that are organized in folders together with textures, lights, etc .
 * <p>
 * E.g. if you have a model in the folder <i>./models/314159/scene.gltf</i> you will get the id <i>{"id":"314159_1",... </i> or with prefix <i>{"id":"prefix_314159_1",...</i>
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
  public Optional<String> next(Path path, MediaType type) {

    if (path == null) {
      return Optional.empty();
    }

    String filename = FilenameUtils.getBaseName(path.toFile().getParentFile().getName());
    String id = prefix ? MediaType.generateId(type, filename) : filename;
    return Optional.of(id);
  }
}
