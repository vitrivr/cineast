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
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

/**
 * A flexible Pathprovider with no caching. Simply stores a list of paths in memory.
 *
 * @author silvan on 19.01.18.
 */
public class SessionContainerProvider implements ExtractionContainerProvider,
    ExtractionCompleteListener {

  private static Logger LOGGER = LogManager.getLogger();
  private volatile boolean open = true;
  private List<ExtractionItemContainer> buffer = new ArrayList<>();
  private Gauge pathsInQueue;
  private Counter pathsCompleted;
  private static final AtomicInteger queueNumber = new AtomicInteger();
  private final int instance;
  private volatile boolean closing = false;

  public SessionContainerProvider() {
    if (Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.debug("Enabling prometheus monitoring for paths in queue {}", queueNumber.get());
      instance = queueNumber.getAndIncrement();
      pathsInQueue = Gauge.build().name("cineast_item_in_queue_" + instance)
          .help("Paths currently in Queue " + instance).register();
      pathsCompleted = Counter.build().name("cineast_item_completed_queue_" + instance)
          .help("Paths completed in Queue " + instance).register();
    } else {
      instance = 0;
    }
  }

  /**
   * Delayed close. After every item has been taken from the buffer, the instance will report itself as closed.
   */
  public void endSession(){
    closing = true;
  }

  @Override
  public void close() {
    LOGGER.debug("Closing SessionPathProvider");
    open = false;
    closing = true;
  }

  @Override
  public void addPaths(List<ExtractionItemContainer> pathList) {
    if(!open){
      LOGGER.debug("Closed, discarding paths.");
      return;
    }
    buffer.addAll(pathList);
    if (pathsInQueue != null) {
      pathsInQueue.inc(pathList.size());
    }
  }

  @Override
  public boolean isOpen() {
    return open && (buffer.size()!=0 || !closing);
  }

  @Override
  public boolean hasNextAvailable() {
    return buffer.size() != 0 && open;
  }

  @Override
  public synchronized Optional<ExtractionItemContainer> next() {
    if (buffer.size() != 0 && open) {
      if (pathsInQueue != null) {
        pathsInQueue.dec();
      }
      return Optional.of(buffer.remove(0));
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
