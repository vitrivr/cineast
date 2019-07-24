package org.vitrivr.cineast.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.fasterxml.jackson.databind.JsonNode;

public final class MetadataUtil {
  private MetadataUtil() {}

  private static final String JSON_EXTENSION = "json";

  private static final Logger logger = LogManager.getLogger();
  private static final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

  /**
   * Reads the {@link Metadata} from the given {@link Path} and returns the first {@link Directory}
   * of the specified type, if present.
   *
   * <p>Note that this is an utility method when one is interested in only one specific
   * {@code Directory}. Use {@code Metadata} and its factory methods
   * (e.g. {@link ImageMetadataReader#readMetadata} if multiple directories or more fine-graded
   * control is needed.
   *
   * @param path a path from which the directory may be read.
   * @param directoryType the {@code Directory} type
   * @param <T> the {@code Directory} type
   * @return an {@link Optional} containing the first {@code Directory} of type {@code T} of the
   *         metadata of the file, if present, otherwise an empty {@code Optional}.
   */
  public static <T extends Directory> T getMetadataDirectoryOfType(Path path,
      Class<T> directoryType) {
    Metadata metadata = null;
    try {
      metadata = ImageMetadataReader.readMetadata(path.toFile());
    } catch (ImageProcessingException | IOException e) {
      logger.error("Error while reading exif data of file {}: {}",
          path, LogHelper.getStackTrace(e));
    }
    if(metadata==null){
      return null;
    }
    return metadata.getFirstDirectoryOfType( directoryType );
  }

  /**
   * Extracts the JSON tree structure as a {@link JsonNode} of the accompanying JSON metadata file
   * named after the given file. That is, both files have the same name without the respective
   * extension, e.g. {@code image_00001.json} for {@code image_00001.jpg}.
   *
   * @param objectPath the path to the original file
   * @return an {@link Optional} containing the JSON as a {@code JsonNode}, if available and valid,
   *         otherwise an empty {@code Optional}.
   */
  public static Optional<JsonNode> getJsonMetadata(Path objectPath) {
    String fileName = objectPath.getFileName().toString();
    String fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
    Path metadataPath = objectPath.resolveSibling(fileNameWithoutExtension + '.' + JSON_EXTENSION);

    if (Files.notExists(metadataPath)) {
      logger.info("JSON file {} for file {} does not exist.", metadataPath, objectPath);
      return Optional.empty();
    }

    return Optional.ofNullable(jsonProvider.toJsonNode(metadataPath));
  }
}
