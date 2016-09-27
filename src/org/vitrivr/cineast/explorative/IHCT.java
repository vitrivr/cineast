package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCT<T extends Comparable<T>> {

    void insert(T nextItem) throws Exception;

    IHCTCell<T> preemptiveCellSearch(List<IHCTCell<T>> ArrayCS, T nextItem, int curLevelNo, int levelNo) throws Exception;

    void remove(IHCTCell<T> cellO, T value, int levelNo) throws Exception;
}
