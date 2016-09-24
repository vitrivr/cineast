package org.vitrivr.cineast.explorative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public class MSTNode<T> implements IMSTNode<T>, Serializable {

    private List<T> value;
    private Mathematics mathematics;

    public MSTNode(List<T> value, Mathematics mathematics){
        this.value = value;
        this.mathematics = mathematics;
    }

    @Override
    public double distance(IMSTNode<T> other){
        return mathematics.getEuclideanDistance(value, other.getValue());
    };

    public double distance(List<T> otherValues){
        return mathematics.getEuclideanDistance(value, otherValues);
    };


    @Override
    public List<T> getValue() {
        return value;
    }

    @Override
    public String toString(){
        return String.format("MSTNode | value: %s >", Utils.listToString(value));
    }

}
