package org.vitrivr.cineast.explorative;

import java.io.Serializable;
import java.util.List;

/**
 * Created by silvanstich on 05.10.16.
 */
public interface TreeTraverserHorizontal<T> extends Serializable {

    void start();

    void newLevel();

    void newCell();

    void processValues(List<T> values, T representativeValue, T parentRepresentativeValue);

    void endCell();

    void endLevel(int levelNo);

    void finished();
}
