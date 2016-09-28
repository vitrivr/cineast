package org.vitrivr.cineast.core.data.hct;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public interface CompactnessCalculation {

    double getCompactness(SimpleWeightedGraph<?, DefaultWeightedEdge> graph);
}
