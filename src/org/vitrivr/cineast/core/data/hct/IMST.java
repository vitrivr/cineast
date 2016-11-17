package org.vitrivr.cineast.core.data.hct;

import java.util.List;


interface IMST<T extends Comparable<T>> {

    void add(T item) throws Exception;

    void remove(T item) throws Exception;

    IMSTNode<T> getNucleus() throws Exception;

    double getCompactness() throws Exception;

    double getCoveringRadius() throws Exception;

    boolean isReadyForMitosis();

    List<IMST<T>> mitosis() throws Exception;

    boolean isCellDead();

    boolean containsValue(T value);

    List<T> getValues();
}
