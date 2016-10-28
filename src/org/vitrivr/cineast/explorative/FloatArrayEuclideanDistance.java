package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.hct.DistanceCalculation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FloatArrayEuclideanDistance implements DistanceCalculation<HCTFloatVectorValue>, Serializable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static transient Map<HCTFloatVectorValue, Map<HCTFloatVectorValue, Double>> cache = new HashMap<>(1000000);
    public static int cacheCounter = 0;
    public static int calculationCounter = 0;

    @Override
    public double distance(HCTFloatVectorValue point1, HCTFloatVectorValue point2) {


        if (isCached(point1, point2)){
            return getVDistFromCache(point1, point2);
        }
        if (isCached(point2, point1)){
            return getVDistFromCache(point2, point1);
        }

        calculationCounter++;
        double dist = 0;
        float[] vector1 = point1.getVector();
        float[] vector2 = point2.getVector();
        for(int i = 0; i < vector1.length; i++){
            dist += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }

        storeInCache(point1, point2, dist);

        return dist;
    }

    private static boolean isCached(HCTFloatVectorValue point1, HCTFloatVectorValue point2) {
        if(cache.containsKey(point1)){
            if(cache.get(point1).containsKey(point2)){
                return true;
            }
        }
        return false;
    }

    private static double getVDistFromCache(HCTFloatVectorValue point1, HCTFloatVectorValue point2){
        cacheCounter++;
        return cache.get(point1).get(point2);
    }

    private static void storeInCache(HCTFloatVectorValue point1, HCTFloatVectorValue point2, double dist) {
        if(!cache.containsKey(point1)){
            cache.put(point1, new HashMap<>());
        }
        cache.get(point1).put(point2, dist);
    }
}
