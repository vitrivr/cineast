package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
interface IMST<V> {

    void add(List<V> item);

    void remove(List<V> item);

    MSTNode<V> getNucleus();

    double getCompactness();

    double getCoveringRadius();

    boolean isReadyForMitosis();

    List<MST<V>> mitosis();

    boolean isCellDeath();

    boolean containsValue(List<V> value);
}
