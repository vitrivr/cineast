package org.vitrivr.cineast.core.data.hct;


import java.util.List;

public interface DistanceCalculation<T> {

    double distance(T point1, T point2);

}
