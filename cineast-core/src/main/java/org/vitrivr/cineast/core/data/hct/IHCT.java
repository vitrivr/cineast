package org.vitrivr.cineast.core.data.hct;

import java.util.List;

public interface IHCT<T extends Comparable<T>> {

    void insert(T nextItem) throws Exception;

    IHCTCell<T> preemptiveCellSearch(List<IHCTCell<T>> ArrayCS, T nextItem, int curLevelNo, int levelNo) throws Exception;

    void remove(IHCTCell<T> cellO, T value, int levelNo) throws Exception;
}
