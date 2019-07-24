package org.vitrivr.cineast.core.data.hct;

interface IMSTNode<T extends Comparable<T>>  {

    double distance(IMSTNode<T> other);

    double distance(T otherValue);

    T getValue();


}
