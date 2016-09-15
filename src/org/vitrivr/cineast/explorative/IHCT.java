package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCT<T> {

    void insert(List<T> nextItem, int levelNo);

    HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, List<T> nextItem, int curLevelNo, int levelNo);

    void remove(List<T> deleteItem, int levelNo);
}
