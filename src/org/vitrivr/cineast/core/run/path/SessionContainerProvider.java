package org.vitrivr.cineast.core.run.path;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

/**
 * A flexible Pathprovider with no caching. Simply stores a list of paths in memory. Differentiates
 * between three states - running, closing and closed(=!open). This is necessary because when a
 * Session is ended by the user, he still expects the submitted items to be extracted. Therefore, on
 * an {@link #endSession()} call, the instance is only {@link #closing}, but still {@link #open}.
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
  private Lock stateModification = new ReentrantLock();

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
   * Delayed close. After every item has been taken from the buffer, the instance will report itself
   * as closed.
   */
  public void endSession() {
    stateModification.lock();
    LOGGER.debug("Received request to end session, closing");
    closing = true;
    stateModification.unlock();
  }

  @Override
  public void close() {
    stateModification.lock();
    LOGGER.debug("Closing SessionPathProvider completely");
    open = false;
    closing = true;
    stateModification.unlock();
  }

  @Override
  public void addPaths(List<ExtractionItemContainer> pathList) {
    stateModification.lock();
    if (!open) {
      LOGGER.debug("Closed, discarding paths.");
      stateModification.unlock();
      return;
    }
    LOGGER.debug("Adding {} paths", pathList.size());
    buffer.addAll(pathList);
    if (pathsInQueue != null) {
      pathsInQueue.inc(pathList.size());
    }
    stateModification.unlock();
  }

  @Override
  public boolean isOpen() {
    stateModification.lock();
    boolean res = open && (buffer.size() != 0 || !closing);
    if (!res) {
      LOGGER.debug(
          "Provider is not open, has a buffer size of {} and is closing, informing about not being open anymore",
          buffer.size());
    }
    stateModification.unlock();
    return res;
  }

  @Override
  public boolean hasNextAvailable() {
    stateModification.lock();
    boolean res = buffer.size() != 0 && open;
    stateModification.unlock();
    return res;
  }

  @Override
  public synchronized Optional<ExtractionItemContainer> next() {
    stateModification.lock();
    if (buffer.size() != 0 && open) {
      if (pathsInQueue != null) {
        pathsInQueue.dec();
      }
      stateModification.unlock();
      return Optional.of(buffer.remove(0));
    }
    stateModification.unlock();
    return Optional.empty();
  }

  @Override
  public void onCompleted(ExtractionItemContainer path) {
    if (pathsCompleted != null) {
      pathsCompleted.inc();
    }
  }

  /**
   * Tells the container to not mark this instance as closing, no matter what the previous state
   * was.
   */
  public boolean keepAliveCheckIfClosed() {
    stateModification.lock();
    if (!open) {
      LOGGER.debug("Provider is already closed, cannot keep it running");
      return true;
    }
    LOGGER.trace("Received keepAlive");
    closing = false;
    stateModification.unlock();
    return false;
  }
}
