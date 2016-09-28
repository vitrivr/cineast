package org.vitrivr.cineast.core.data.hct;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.vitrivr.cineast.core.data.hct.CompactnessCalculation;

import java.io.Serializable;

public class DefaultCompactnessCalculation implements CompactnessCalculation, Serializable{

    @Override
    public double getCompactness(SimpleWeightedGraph<?, DefaultWeightedEdge> graph) {
        double fullWeight = 0;

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            fullWeight += graph.getEdgeWeight(edge);
        }
        return graph.vertexSet().size() > 30 && fullWeight / graph.vertexSet().size() > 20 ? 1.0d : 0.0d;
    }
}
