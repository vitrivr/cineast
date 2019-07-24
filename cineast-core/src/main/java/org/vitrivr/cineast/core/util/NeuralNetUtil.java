package org.vitrivr.cineast.core.util;

/**
 * Created by silvan on 09.09.16.
 */
public class NeuralNetUtil {


    /**
     * Takes the maximum value at each position
     */
    public static float[] maxpool(float[] curr, float[] probs) {
        if(curr.length!=probs.length){
            throw new IllegalArgumentException("Float[] need to have the same size");
        }
        float[] _ret = new float[curr.length];
        for(int i = 0; i<curr.length;i++){
            if(curr[i]>probs[i]){
                _ret[i]= curr[i];
            } else{
                _ret[i]=probs[i];
            }
        }
        return _ret;
    }
}
