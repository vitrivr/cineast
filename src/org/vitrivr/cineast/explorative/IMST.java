package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
interface IMST<T extends Comparable<T> & DistanceCalculation<T>> {

    void add(T item);

    void remove(T item);

    MSTNode<T> getNucleus() throws Exception;

    double getCompactness() throws Exception;

    double getCoveringRadius() throws Exception;

    boolean isReadyForMitosis();

    List<MST<T>> mitosis();

    boolean isCellDeath();

    boolean containsValue(T value);

    List<T> getValues();
}
