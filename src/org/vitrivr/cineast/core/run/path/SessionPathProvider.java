package org.vitrivr.cineast.core.run.path;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;

/**
 * A flexible Pathprovider with no caching. Simply stores a list of paths in memory.
 *
 * @author silvan on 19.01.18.
 */
public class SessionPathProvider implements ExtractionPathProvider,
    ExtractionCompleteListener {

  private static Logger LOGGER = LogManager.getLogger();
  private volatile boolean open = true;
  private List<Path> pathBuffer = new ArrayList<>();
  private Gauge pathsInQueue;
  private Counter pathsCompleted;
  private static final AtomicInteger queueNumber = new AtomicInteger();
  private final int instance;

  public SessionPathProvider() {
    if (Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.debug("Enabling prometheus monitoring for paths in queue {}", queueNumber.get());
      instance = queueNumber.getAndIncrement();
      pathsInQueue = Gauge.build().name("cineast_paths_in_queue_" + instance)
          .help("Paths currently in Queue " + instance).register();
      pathsCompleted = Counter.build().name("cineast_path_completed_queue_" + instance)
          .help("Paths completed in Queue " + instance).register();
    } else {
      instance = 0;
    }
  }

  @Override
  public void close() {
    LOGGER.debug("Closing SessionPathProvider");
    open = false;
  }

  @Override
  public void addPaths(List<Path> pathList) {
    pathBuffer.addAll(pathList);
    if (pathsInQueue != null) {
      pathsInQueue.inc(pathList.size());
    }
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean hasNextAvailable() {
    return pathBuffer.size() != 0 && open;
  }

  @Override
  public synchronized Optional<Path> next() {
    if (pathBuffer.size() != 0 && open) {
      if (pathsInQueue != null) {
        pathsInQueue.dec();
      }
      return Optional.of(pathBuffer.remove(0));
    }
    return Optional.empty();
  }

  @Override
  public void onCompleted(Path path) {
    if (pathsCompleted != null) {
      pathsCompleted.inc();
    }
  }
}
