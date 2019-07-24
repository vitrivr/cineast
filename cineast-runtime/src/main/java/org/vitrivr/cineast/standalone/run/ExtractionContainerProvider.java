package org.vitrivr.cineast.standalone.run;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;

/**
 * The {@link ExtractionContainerProvider} provides a continuous list of {@link
 * ExtractionItemContainer}. It is intended to be both used for e.g. walking a directory or during
 * an extraction-session using Cineast's API
 *
 * @author silvan on 19.01.18.
 */
public interface ExtractionContainerProvider {

  /**
   * Close this instance. It stops accepting paths, and does not hand out new paths
   */
  void close();

  void addPaths(List<ExtractionItemContainer> pathList);

  default void addPath(ExtractionItemContainer path) {
    this.addPaths(Lists.newArrayList(path));
  }

  /**
   * Check if this instance is still active. This does NOT mean that {@link #hasNextAvailable()}
   * will return true, but simply that there might be elements yet to come.
   */
  boolean isOpen();

  /**
   * Check if {@link #next()} could return an element. Since a {@link ExtractionContainerProvider}
   * might be used in a multi-threaded context, it does not guarantee that by the time you will call
   * {@link #next()}, the element will still be available.
   */
  boolean hasNextAvailable();


  /**
   * @return the next element and its corresponding type if one is available
   */
  Optional<ExtractionItemContainer> next();

}
