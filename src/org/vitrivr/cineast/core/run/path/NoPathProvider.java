package org.vitrivr.cineast.core.run.path;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;

/**
 * Convenience Method when you don't want to actually provide Elements.
 *
 * @author silvan on 19.01.18.
 */
public class NoPathProvider implements ExtractionPathProvider, ExtractionCompleteListener {

  @Override
  public void onCompleted(Path path) {
    //Ignore
  }

  @Override
  public void close() {
    //Ignore
  }

  @Override
  public void addPaths(List<Path> pathList) {
    //Ignore
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public boolean hasNextAvailable() {
    return false;
  }

  @Override
  public Optional<Path> next() {
    return Optional.empty();
  }
}
