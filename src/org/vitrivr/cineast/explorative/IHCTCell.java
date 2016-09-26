package org.vitrivr.cineast.explorative;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCTCell<T extends Comparable<T> & DistanceCalculation<T>> {

    void addChild(HCTCell<T> child);

    boolean containsValue(T value);

    boolean isCellDeath();

    void removeChild(HCTCell<T> child);
}
