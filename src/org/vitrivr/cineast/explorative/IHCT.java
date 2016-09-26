package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCT<T extends Comparable<T> & DistanceCalculation<T>> {

    void insert(T nextItem) throws Exception;

    HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, T nextItem, int curLevelNo, int levelNo) throws Exception;

    void remove(HCTCell<T> cellO, T value, int levelNo) throws Exception;
}
