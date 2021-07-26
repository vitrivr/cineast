package org.vitrivr.cineast.standalone.run.path;

import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


public class ExtractionContainerProviderFactory {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Tries to create a {@link TreeWalkContainerIteratorProvider}. Will however create a {@link
   * NoContainerProvider} if something goes wrong.
   */
  public static ExtractionContainerProvider tryCreatingTreeWalkPathProvider(File jobFile, ExtractionContextProvider context) {
        /* Check if context could be read and an input path was specified. */
    if (context == null || !context.inputPath().isPresent()) {
      return new NoContainerProvider();
    }
    Path jobDirectory = jobFile.getAbsoluteFile().toPath().getParent();
    if (!context.inputPath().isPresent()) {
      return new NoContainerProvider();
    }
    Path inputPath = context.inputPath().get();
    Path basePath;
    Path startPath;
    if (context.relPath().isPresent()) {
      basePath = context.relPath().get();
      startPath = inputPath;
    } else {
      basePath = inputPath;
      startPath = Paths.get("");
    }
    basePath = jobDirectory.resolve(basePath).normalize().toAbsolutePath();

    if (!Files.exists(basePath)) {
      LOGGER.warn("The path '{}' specified in the extraction configuration does not exist!",
          basePath.toString());
      return new NoContainerProvider();
    }

    return new TreeWalkContainerIteratorProvider(basePath, startPath, context.depth());
  }

}
