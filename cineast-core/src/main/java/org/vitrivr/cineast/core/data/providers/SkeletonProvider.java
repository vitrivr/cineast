package org.vitrivr.cineast.core.data.providers;

import java.util.Collections;
import java.util.List;
import org.vitrivr.cineast.core.data.Skeleton;

public interface SkeletonProvider {

  default List<Skeleton> getSkeletons() {
    return Collections.emptyList();
  }

}
