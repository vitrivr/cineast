package org.vitrivr.cineast.core.util.images;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;

public final class MetadataUtil {
  private MetadataUtil() {}

  private static final Logger logger = LogManager.getLogger();

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
  public static <T extends Directory> Optional<T> getMetadataDirectoryOfType(Path path, Class<T> directoryType) {
    Optional<Metadata> metadata = Optional.empty();
    try {
      metadata = Optional.of(ImageMetadataReader.readMetadata(path.toFile()));
    } catch (ImageProcessingException | IOException e) {
      logger.error("Error while reading exif data of file {}: {}", path, LogHelper.getStackTrace(e));
    }
    return metadata.map(m -> m.getFirstDirectoryOfType(directoryType));
  }
}
