package org.vitrivr.cineast.explorative;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCT<T> {

    void insert(T nextItem, int levelNo);

    void preemptiveCellSearch(T[] ArrayCS, T nextItem, int curLevelNo);

    T remove(T[] ArrayIR, int levelNo);
}
