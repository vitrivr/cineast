package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IMST<V> {

    void add(List<V> item);

    void remove(List<V> item);

    List<V> getNucleus();

}
