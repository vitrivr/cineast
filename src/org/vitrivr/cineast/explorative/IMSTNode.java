package org.vitrivr.cineast.explorative;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IMSTNode<T extends DistanceCalculation<T>>  {

    double distance(IMSTNode<T> other);

    T getValue();


}
