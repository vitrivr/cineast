package org.vitrivr.cineast.standalone.run.path;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * Convenience Provider for the {@link org.vitrivr.cineast.core.config.IngestConfig}
 *
 * @author silvan on 19.01.18.
 */
public class SingletonContainerProvider implements ExtractionContainerProvider,
    ExtractionCompleteListener {

  private final Path path;
  private volatile boolean open = true;

  public SingletonContainerProvider(Path path) {
    this.path = path;
  }

  @Override
  public void onCompleted(ExtractionItemContainer path) {
    open = false;
  }

  @Override
  public void close() {
    open = false;
  }

  @Override
  public void addPaths(List<ExtractionItemContainer> pathList) {
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
  public synchronized Optional<ExtractionItemContainer> next() {
    open = false;
    return Optional.of(new ExtractionItemContainer(new MediaObjectDescriptor(path.getFileName()), null, path));
  }
}
