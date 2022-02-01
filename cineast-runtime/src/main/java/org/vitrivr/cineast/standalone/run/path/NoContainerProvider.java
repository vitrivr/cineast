package org.vitrivr.cineast.standalone.run.path;

import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.standalone.run.ExtractionCompleteListener;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * Convenience Method when you don't want to actually provide Elements.
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
