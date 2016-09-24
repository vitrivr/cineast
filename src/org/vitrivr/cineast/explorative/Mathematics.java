package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.Set;

/**
 * Created by silvanstich on 24.09.16.
 */
public interface Mathematics {

    double getEuclideanDistance(List point1, List point2);

    int compareVectors(List vector1, List vector2);

    double calculateCompactness(Set mstNodes, Set<DefaultWeightedEdge> defaultWeightedEdges, SimpleWeightedGraph graph);
}
