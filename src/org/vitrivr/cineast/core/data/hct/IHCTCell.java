package org.vitrivr.cineast.core.data.hct;

import java.util.List;

interface IHCTCell<T extends Comparable<T>> {

    void addChild(IHCTCell<T> child);

    boolean containsValue(T value);

    boolean isCellDeath();

    void removeChild(IHCTCell<T> child);

    IMSTNode<T> getNucleus() throws Exception;

    void setParent(IHCTCell<T> newCell);

    double getDistanceToNucleus(T nextItem) throws Exception;

    List<IHCTCell<T>> getChildren();

    IHCTCell<T> getParent();

    void addValue(T nextItem);

    boolean isReadyForMitosis();

    <T extends Comparable<T>> List<HCTCell<T>> mitosis() throws Exception;

    void removeValue(T value);

    double getCoveringRadius() throws Exception;

    List<T> getValues();
}

