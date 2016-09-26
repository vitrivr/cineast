package org.vitrivr.cineast.explorative;

import java.io.Serializable;

/**
 * Created by silvanstich on 13.09.16.
 */
public class MSTNode<T extends Comparable<T> & DistanceCalculation<T>> implements IMSTNode<T>, Serializable {

    private T value;

    public MSTNode(T value){
        this.value = value;
    }

    @Override
    public double distance(IMSTNode<T> other){
        return value.distance(other.getValue());
    };

    public double distance(T otherValue){
        return value.distance(otherValue);
    };


    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString(){
        return String.format("MSTNode | value: %s >", value.toString());
    }

}
