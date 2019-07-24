package org.vitrivr.cineast.standalone.run.path;

import io.prometheus.client.Counter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/*
 * Recursively add all files under that path to the List of files that should be processed. Uses
 * the context-provider to determine the depth of recursion, skip files and limit the number of
 * files.
 */
public class TreeWalkContainerIteratorProvider implements ExtractionContainerProvider,
    ExtractionCompleteListener {

  private static final Logger LOGGER = LogManager.getLogger();

  private final Path basePath;
  private final ExtractionContextProvider context;
  private volatile boolean open = true;
  private Iterator<Path> pathIterator = Collections.emptyIterator();
  private Counter pathsCompleted;

  public TreeWalkContainerIteratorProvider(Path basePath, ExtractionContextProvider context) {
    this.basePath = basePath;
    this.context = context;
    if (Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.debug("Enabling prometheus monitoring for paths in queue");
      pathsCompleted = Counter.build().name("cineast_path_completed_treewalk")
          .help("Paths completed in Tree Walk for base path " + basePath).register();
    }
    try {
      pathIterator = Files.walk(this.basePath, this.context.depth(), FileVisitOption.FOLLOW_LINKS)
          .filter(p -> {
            try {
              return Files.exists(p) && Files.isRegularFile(p) && !Files.isHidden(p)
                  && Files.isReadable(p);
            } catch (IOException e) {
              LOGGER.error("An IO exception occurred while testing the media file at '{}': {}",
                  p.toString(),
                  LogHelper.getStackTrace(e));
              return false;
            }
          }).iterator();
    } catch (IOException e) {
      LOGGER.error("An IO exception occurred while scanning '{}': {}", basePath.toString(),
          LogHelper.getStackTrace(e));
    }
  }

  @Override
  public void close() {
    open = false;
  }

  @Override
  public void addPaths(List<ExtractionItemContainer> pathList) {
    LOGGER.error("Cannot add paths to a TreeWalkPathIterator");
  }

  /**
   * Since no elements are added to the iterator, this provider is also closed when the iterator
   * does not have further elements.
   */
  @Override
  public boolean isOpen() {
    return pathIterator.hasNext() && open;
  }

  @Override
  public boolean hasNextAvailable() {
    return pathIterator.hasNext() && open;
  }

  @Override
  public synchronized Optional<ExtractionItemContainer> next() {
    if (pathIterator.hasNext() && open) {
      Path next = pathIterator.next();
      Path path = basePath.toFile().isDirectory() ? basePath.toAbsolutePath()
          .relativize(next.toAbsolutePath()) : next.getFileName();
      LOGGER.debug("Next path: {}, base {}, res {}", next, basePath, path);
      return Optional.of(new ExtractionItemContainer(new MediaObjectDescriptor(path), null, next));
    }
    return Optional.empty();
  }

  @Override
  public void onCompleted(ExtractionItemContainer path) {
    if (pathsCompleted != null) {
      pathsCompleted.inc();
    }
  }
}
