package org.vitrivr.cineast.explorative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public class MSTNode<T> implements IMSTNode<T> {

    private List<T> value;
    private MST mst;


    public MSTNode(List<T> value, MST mst){
        this.value = value;
        this.mst = mst;
    }

    @Override
    public double distance(IMSTNode<T> other, Function<List<List<T>>, Double> calculation){
        return distance(other.getValue(), calculation);
    };

    public double distance(List<T> otherValues, Function<List<List<T>>, Double> calculation){
        List<List<T>> args = new ArrayList<>();
        args.add(value); args.add(otherValues);
        return calculation.apply(args);
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
