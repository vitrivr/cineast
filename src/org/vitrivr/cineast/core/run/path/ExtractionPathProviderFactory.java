package org.vitrivr.cineast.core.run.path;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;

/**
 * @author silvan on 19.01.18.
 */
public class ExtractionPathProviderFactory {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Tries to create a {@link TreeWalkPathIteratorProvider}. Will however create a {@link
   * NoPathProvider} if something goes wrong.
   */
  public static ExtractionPathProvider tryCreatingTreeWalkPathProvider(File jobFile,
      ExtractionContextProvider context) {
        /* Check if context could be read and an input path was specified. */
    if (context == null || context.inputPath() == null || context.pathProvider() == null) {
      return new NoPathProvider();
    }
    Path jobDirectory = jobFile.getAbsoluteFile().toPath().getParent();
    if(!context.inputPath().isPresent()){
      return new NoPathProvider();
    }
    Path path = jobDirectory.resolve(context.inputPath().get()).normalize().toAbsolutePath();

    if (!Files.exists(path)) {
      LOGGER.warn("The path '{}' specified in the extraction configuration does not exist!",
          path.toString());
      return new NoPathProvider();
    }

    return new TreeWalkPathIteratorProvider(path, context);
  }

}
