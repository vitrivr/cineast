package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public abstract class MSTNode<T extends Number> implements IMSTNode<T> {

    private List<T> value;
    private MST mst;


    public MSTNode(List<T> value, MST mst){
        this.value = value;
        this.mst = mst;
    }

    @Override
    public abstract double distance(IMSTNode<T> other);

    @Override
    public List<T> getValue() {
        return value;
    }
}
