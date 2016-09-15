package org.vitrivr.cineast.explorative;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IMSTNode<T> {

    double distance(IMSTNode<T> other, Function<List<List<T>>, Double> calculation);

    List<T> getValue();


}
