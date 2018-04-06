package org.vitrivr.cineast.core.run.path;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;

/**
 * Convenience Provider for the {@link org.vitrivr.cineast.core.config.IngestConfig}
 *
 * @author silvan on 19.01.18.
 */
public class SingletonPathProvider implements ExtractionPathProvider, ExtractionCompleteListener {

  private final Path path;
  private volatile boolean open = true;

  public SingletonPathProvider(Path path) {
    this.path = path;
  }

  @Override
  public void onCompleted(Path path) {
    open = false;
  }

  @Override
  public void close() {
    open = false;
  }

  @Override
  public void addPaths(List<Path> pathList) {
    //Ignore
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean hasNextAvailable() {
    return open;
  }

  @Override
  public synchronized Optional<Path> next() {
    open = false;
    return Optional.of(path);
  }
}
