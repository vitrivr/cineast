package org.vitrivr.cineast.explorative;

import java.io.Serializable;


public class HCTFloatVectorValue
        implements Comparable<HCTFloatVectorValue>,
        Serializable, Printable{

    private final float[] vector;
    private final String id;

    public HCTFloatVectorValue(float[] vector, String id) {
        this.vector = vector;
        this.id = id;
    }

    public float[] getVector() {
        return vector;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(HCTFloatVectorValue o) {
        if(id.hashCode() > o.getId().hashCode() ){
            return 1;
        }
        if(id.hashCode() < o.getId().hashCode()){
            return -1;
        }
        return 0;
    }

    @Override
    public String printHtml() {
        return "<img class=\"thumb\" src=\"/Applications/XAMPP/xamppfiles/htdocs/vitrivr-ui/thumbnails/" + id + ".jpg\" />";
    }

    @Override
    public String print() {
        return id;
    }
}
