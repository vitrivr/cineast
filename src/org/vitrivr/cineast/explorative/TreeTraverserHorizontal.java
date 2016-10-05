package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 05.10.16.
 */
public interface TreeTraverserHorizontal<T> {

    void newLevel();

    void newCell();

    void processValues(List<T> values, T representative);

    void endCell();

    void endLevel();

}
