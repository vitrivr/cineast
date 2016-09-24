package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by silvanstich on 24.09.16.
 */
public class SimpleFloatMathematics implements Mathematics, Serializable {

    @Override
    public double getEuclideanDistance(List point1, List point2) {
        double distance = 0;
        for (int i = 0; i < point1.size(); i++) {
            distance += ((Float)point1.get(i) - (Float)point2.get(i)) * ((Float)point1.get(i) - (Float)point2.get(i));
        }
        return Math.sqrt(distance);
    }

    @Override
    public int compareVectors(List vector1, List vector2) {
        double lenFirstVector = 0;
        double lenSecondVector = 0;
        for (int i = 0; i < vector1.size(); i++) {
            lenFirstVector += (Float)vector1.get(i) * (Float)vector1.get(i);
            lenSecondVector += (Float)vector2.get(i) * (Float)vector2.get(i);
        }
        if(lenFirstVector > lenSecondVector) {
            return 1;
        } else if (lenSecondVector > lenFirstVector) {
            return -1;
        } else{
            return 0;
        }
    }

    @Override
    public double calculateCompactness(Set vertices, Set<DefaultWeightedEdge> edges, SimpleWeightedGraph graph) {
        double totalWeight = 0;
        for(DefaultWeightedEdge edge : edges){
            totalWeight += graph.getEdgeWeight(edge);
        }
        if(totalWeight > 20 && vertices.size() > 5){
            return 1d;
        } else {
            return 0d;
        }
    }
}
