package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;

/**
 * Created by silvanstich on 26.09.16.
 */
public class HCTFloatVectorValue
        implements Comparable<HCTFloatVectorValue>,
        Serializable, Printable{

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
    public String print() {
        return segment_id;
    }
}
