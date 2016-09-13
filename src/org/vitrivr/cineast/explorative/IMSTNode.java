package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IMSTNode<T> {

    double distance(IMSTNode<T> other);

    List<T> getValue();


}
