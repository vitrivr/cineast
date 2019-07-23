package org.vitrivr.cineast.core.run.path;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

/**
 * Convenience Method when you don't want to actually provide Elements.
 *
 * @author silvan on 19.01.18.
 */
public class NoContainerProvider implements ExtractionContainerProvider, ExtractionCompleteListener {

  @Override
  public void onCompleted(ExtractionItemContainer path) {
    //Ignore
  }

  @Override
  public void close() {
    //Ignore
  }

  @Override
  public void addPaths(List<ExtractionItemContainer> pathList) {
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
  public Optional<ExtractionItemContainer> next() {
    return Optional.empty();
  }
}
