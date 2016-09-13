package org.vitrivr.cineast.explorative;

import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public class MSTNodeDouble extends MSTNode<Double> {

    public MSTNodeDouble(List<Double> value, MST mst) {
        super(value, mst);
    }

    public void setArguments(){

    }

    @Override
    public double distance(IMSTNode<Double> other) {
        Double[] otherValue = (Double[]) other.getValue().toArray();
        Double[] values = (Double[]) this.getValue().toArray();

        double distance = 0;
        for (int i = 0; i < otherValue.length; i++){
            distance += (values[i] - otherValue[i]) *(values[i] - otherValue[i]);
        }

        return distance;
    }
}
