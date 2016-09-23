package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
interface IMST<V> {

    void add(List<V> item);

    void remove(List<V> item);

    MSTNode<V> getNucleus() throws Exception;

    double getCompactness();

    double getCoveringRadius() throws Exception;

    boolean isReadyForMitosis();

    List<MST<V>> mitosis();

    boolean isCellDeath();

    boolean containsValue(List<V> value);

    <T> List<List<T>> getValues();
}
