package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.Skeleton;

import java.util.Collections;
import java.util.List;

public interface SkeletonProvider {

    default List<Skeleton> getSkeletons() {
        return Collections.emptyList();
    }

}
