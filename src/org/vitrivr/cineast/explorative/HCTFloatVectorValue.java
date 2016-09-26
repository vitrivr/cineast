package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;

/**
 * Created by silvanstich on 26.09.16.
 */
public class HCTFloatVectorValue
        implements Comparable<HCTFloatVectorValue>,
        DistanceCalculation<HCTFloatVectorValue>,
        Serializable{

    private final float[] vector;
    private final String segment_id;

    public HCTFloatVectorValue(float[] vector, String segment_id) {
        this.vector = vector;
        this.segment_id = segment_id;
    }

    public float[] getVector() {
        return vector;
    }

    public String getSegment_id() {
        return segment_id;
    }

    @Override
    public int compareTo(HCTFloatVectorValue o) {
        if(segment_id.hashCode() > o.getSegment_id().hashCode() ){
            return 1;
        }
        if(segment_id.hashCode() < o.getSegment_id().hashCode()){
            return -1;
        }
        return 0;
    }

    @Override
    public double distance(HCTFloatVectorValue other) {
        double dist = 0;
        float[] otherVector = other.getVector();

        for(int i = 0; i < vector.length; i++){
            dist += Math.pow(vector[i] - otherVector[i], 2);
        }
        return dist;
    }
}
