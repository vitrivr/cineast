package org.vitrivr.cineast.explorative;

import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.io.Serializable;

/**
 * Created by silvanstich on 27.09.16.
 */
public class FloatArrayEuclideanDistance implements DistanceCalculation<HCTFloatVectorValue>, Serializable {

    @Override
    public double distance(HCTFloatVectorValue point1, HCTFloatVectorValue point2) {
        double dist = 0;
        for(int i = 0; i < point1.getVector().length; i++){
            dist += Math.pow(point1.getVector()[i] - point2.getVector()[i], 2);
        }
        return dist;
    }
}
