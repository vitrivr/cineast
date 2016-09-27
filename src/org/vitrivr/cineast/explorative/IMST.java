package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
interface IMST<T extends Comparable<T>> {

    void add(T item);

    void remove(T item);

    IMSTNode<T> getNucleus() throws Exception;

    double getCompactness() throws Exception;

    double getCoveringRadius() throws Exception;

    boolean isReadyForMitosis();

    List<IMST<T>> mitosis();

    boolean isCellDead();

    boolean containsValue(T value);

    List<T> getValues();
}
