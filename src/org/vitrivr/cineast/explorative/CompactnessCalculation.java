package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Created by silvanstich on 26.09.16.
 */
interface CompactnessCalculation {

    double getCompactness(SimpleWeightedGraph<?, DefaultWeightedEdge> graph);
}
