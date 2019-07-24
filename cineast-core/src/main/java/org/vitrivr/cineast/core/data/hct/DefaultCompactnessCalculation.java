package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class DefaultCompactnessCalculation implements CompactnessCalculation, Serializable{

  private static final long serialVersionUID = -1858648486843864388L;

    @Override
    public double getCompactness(SimpleWeightedGraph<?, DefaultWeightedEdge> graph) {
        double fullWeight = 0;

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            fullWeight += graph.getEdgeWeight(edge);
        }
        return graph.vertexSet().size() > 20 && fullWeight / graph.vertexSet().size() > 0.5 ? 1.0d : 0.0d;
    }
}
