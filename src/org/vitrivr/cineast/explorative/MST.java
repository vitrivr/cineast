package org.vitrivr.cineast.explorative;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.util.*;
import java.util.function.Function;


class MST<V> implements IMST<V> {

    private SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    Function<List<List<V>>, Double> distanceMetric;

    MST(Function<List<List<V>>, Double> distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    @Override
    public void add(List<V> item) {
        MSTNode<V> mstNode = new MSTNode(item, this);
        graph.addVertex(mstNode);
        for (MSTNode<V> node : graph.vertexSet()) {
            if (node != mstNode){
                DefaultWeightedEdge dwe = graph.addEdge(mstNode, node);
                graph.setEdgeWeight(dwe, mstNode.distance(node, distanceMetric));
            }
        }
    }

    @Override
    public void remove(List<V> item) {
        Iterator<MSTNode<V>> iterator = graph.vertexSet().iterator();
        List<MSTNode<V>> toDeleteNodes = new ArrayList<>();
        while(iterator.hasNext()){
            MSTNode<V> current = iterator.next();
            if(current.getValue() == item){
                toDeleteNodes.add(current);
            }
        }
        graph.removeAllVertices(toDeleteNodes);
    }

    public MSTNode<V> getNucleus(){
        if(graph.vertexSet().size() == 1){return (MSTNode<V>) graph.vertexSet().toArray()[0];}
        SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> mst = getMST();
        int degree = 0;
        MSTNode<V> nucleus = null;
        for (MSTNode<V> current : mst.vertexSet()) {
            if (degree < mst.degreeOf(current)) {
                degree = mst.degreeOf(current);
                nucleus = current;
            }
        }
        return nucleus;
    }

    public double getCompactness(){
        // TODO: 14.09.16 implement real compactness measurement
        return graph.vertexSet().size() > 2 ? 1.0 : 0.0;
    }

    public boolean isReadyForMitosis(){
        return getCompactness() > 0.5; // // TODO: 14.09.16 needs real implemenation
    }

    public List<MST<V>> mitosis(){
        SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> mst = getMST();
        double largestWeight = 0;
        DefaultWeightedEdge largestEdge = null;
        for(DefaultWeightedEdge edge : mst.edgeSet()){
            if(mst.getEdgeWeight(edge) > largestWeight){
                largestWeight = mst.getEdgeWeight(edge);
                largestEdge = edge;
            }
        }
        List<MST<V>> newGraphs = new ArrayList<>();
        mst.removeEdge(largestEdge);
        newGraphs.add(createSubGraph(mst, mst.getEdgeSource(largestEdge)));
        newGraphs.add(createSubGraph(mst, mst.getEdgeTarget(largestEdge)));
        return newGraphs;
    }

    @Override
    public boolean isCellDeath() {
        return graph.vertexSet().size() == 0;
    }

    @Override
    public boolean containsValue(List<V> value) { // TODO: 14.09.16 is a bottleneck
        for (MSTNode<V> mstNode : graph.vertexSet()) {
            if(mstNode.getValue() == value) return true;
        }
        return false;
    }

    // covering radius means the distance from the nucleus to the furthest element of the mst
    public double getCoveringRadius(){
        SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> mst = getMST();
        MSTNode<V> nucleus = getNucleus();
        double coveringRadius = 0;
        for(MSTNode<V> node : mst.vertexSet()){
            double pathLength = new DijkstraShortestPath(mst, nucleus, node).getPathLength();
            if(pathLength > coveringRadius) coveringRadius = pathLength;
        }
        return coveringRadius;
    }

    private MST<V> createSubGraph(SimpleWeightedGraph mst, MSTNode<V> startNode){
        List<MSTNode<V>> containedNodes = new ArrayList<>();
        GraphIterator<MSTNode<V>, DefaultWeightedEdge> iterator = new BreadthFirstIterator<>(mst, startNode);
        while(iterator.hasNext()){
            MSTNode<V> next = iterator.next();
            containedNodes.add(next);
        }
        MST<V> newSubGraph = new MST<V>(distanceMetric);
        for(MSTNode<V> node : containedNodes){
            newSubGraph.add(node.getValue());
        }
        return newSubGraph;
    }

    private SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> getMST(){
        MinimumSpanningTree<MSTNode<V>, DefaultWeightedEdge> internMst = new PrimMinimumSpanningTree<>(graph);
        Set<DefaultWeightedEdge> edges = internMst.getMinimumSpanningTreeEdgeSet();
        Set<MSTNode<V>> nodes = graph.vertexSet();
        SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> internSWG = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for(MSTNode<V> node : nodes) internSWG.addVertex(node);
        for(DefaultWeightedEdge dwe : edges){
            DefaultWeightedEdge newEdge = internSWG.addEdge(graph.getEdgeSource(dwe), graph.getEdgeTarget(dwe));
            internSWG.setEdgeWeight(newEdge, graph.getEdgeWeight(dwe));
        }
        return internSWG;
    }

    public String toString(){
        return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(), graph.edgeSet().size(), Utils.listToString(getNucleus().getValue()));
    }
}
