package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Created by silvanstich on 26.09.16.
 */
public class DefaultCompactnessCalculation implements CompactnessCalculation{

    @Override
    public double getCompactness(SimpleWeightedGraph<?, DefaultWeightedEdge> graph) {
        double fullWeight = 0;

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            fullWeight += graph.getEdgeWeight(edge);
        }


        return graph.vertexSet().size() > 30 && fullWeight / graph.vertexSet().size() > 20 ? 1.0d : 0.0d;
    }
}
