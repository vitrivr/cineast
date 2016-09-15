package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public interface IHCTCell<T> {

    boolean containsValue(List<T> value);

    boolean isCellDeath();

    void setChild(HCTCell<T> child);

    void removeChild(HCTCell<T> child);
}
