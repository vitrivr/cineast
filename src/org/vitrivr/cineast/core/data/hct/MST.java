package org.vitrivr.cineast.core.data.hct;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.io.Serializable;
import java.util.*;


class MST<T extends Comparable<T>> implements IMST<T>, Serializable {

    private final HCT<T> hct;

    private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> mst;
    private MSTNode<T> nucleus;
    private double coveringRadius;

    MST(HCT<T> hct) {
        this.hct = hct;
    }

    @Override
    public void add(T item) throws Exception {
        MSTNode<T> mstNode = new MSTNode<>(item, hct);
        graph.addVertex(mstNode);
        for (MSTNode<T> node : graph.vertexSet()) {
            if (node != mstNode){
                DefaultWeightedEdge dwe = graph.addEdge(mstNode, node);
                graph.setEdgeWeight(dwe, mstNode.distance(node));
            }
        }
        mst = getMST();
        nucleus = updateNucleus();
        coveringRadius = updateCoveringRadius();
    }

    @Override
    public void remove(T item) throws Exception {
        Iterator<MSTNode<T>> iterator = graph.vertexSet().iterator();
        List<MSTNode<T>> toDeleteNodes = new ArrayList<>();
        while(iterator.hasNext()){
            MSTNode<T> current = iterator.next();
            if(current.getValue() == item){
                toDeleteNodes.add(current);
            }
        }
        graph.removeAllVertices(toDeleteNodes);
        mst = getMST();
        if(mst.vertexSet().size() > 0){
            nucleus = updateNucleus();
            coveringRadius = updateCoveringRadius();
        }
    }

    @Override
    public MSTNode<T> getNucleus(){
        return nucleus;
    }

    private MSTNode<T> updateNucleus() throws java.lang.Exception{
        if(graph.vertexSet().size() == 0) throw new Exception("This graph contains no nodes!");
        if(graph.vertexSet().size() == 1){
            return (MSTNode<T>) graph.vertexSet().toArray()[0];}
        int degree = 0;
        MSTNode<T> nucleus = null;
        List<MSTNode<T>> mstNodes = new ArrayList<>();
        mstNodes.addAll(mst.vertexSet());
        Collections.sort(mstNodes, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        for (MSTNode<T> current : mstNodes) {
            if (degree < mst.degreeOf(current)) {
                degree = mst.degreeOf(current);
                nucleus = current;
            }
        }
        return nucleus;
    }

    public double getCompactness() {
        return hct.getCompactnessCalculation().getCompactness(graph);
    }

    public boolean isReadyForMitosis(){

        return getCompactness() > 0.5; // // TODO: 14.09.16 needs real implemenation
    }

    public List<IMST<T>> mitosis() throws Exception {
        double largestWeight = 0;
        DefaultWeightedEdge largestEdge = null;
        List<DefaultWeightedEdge> edges = new ArrayList<>();
        edges.addAll(mst.edgeSet());
        Collections.sort(edges, (el1, el2) -> graph.getEdgeSource(el1).getValue().compareTo(graph.getEdgeSource(el2).getValue()));
        for(DefaultWeightedEdge edge : edges){
            if(mst.getEdgeWeight(edge) > largestWeight){
                largestWeight = mst.getEdgeWeight(edge);
                largestEdge = edge;
            }
        }
        List<IMST<T>> newGraphs = new ArrayList<>();
        mst.removeEdge(largestEdge);
        newGraphs.add(createSubGraph(mst, mst.getEdgeSource(largestEdge)));
        newGraphs.add(createSubGraph(mst, mst.getEdgeTarget(largestEdge)));
        return newGraphs;
    }

    @Override
    public boolean isCellDead() {
        return graph.vertexSet().isEmpty();
    }

    @Override
    public boolean containsValue(T value) {
        for (MSTNode<T> mstNode : graph.vertexSet()) {
            if(mstNode.getValue() == value) return true;
        }
        return false;
    }

    @Override
    public List<T> getValues() {
        List<T> values = new ArrayList<>();
        for(MSTNode<T> node : graph.vertexSet()){
            values.add(node.getValue());
        }
        return values;
    }

    public double getCoveringRadius() throws Exception{
        return coveringRadius;
    }

    // covering radius means the distance from the nucleus to the furthest element of the mst
    private double updateCoveringRadius() throws Exception{
        double coveringRadius = 0;
        for(MSTNode<T> node : mst.vertexSet()){
            double pathLength = new DijkstraShortestPath(mst, nucleus, node).getPathLength();
            if(pathLength > coveringRadius) coveringRadius = pathLength;
        }
        return coveringRadius;
    }

    private MST<T> createSubGraph(SimpleWeightedGraph mst, MSTNode<T> startNode) throws Exception {
        List<MSTNode<T>> containedNodes = new ArrayList<>();
        GraphIterator<MSTNode<T>, DefaultWeightedEdge> iterator = new BreadthFirstIterator<>(mst, startNode);
        while(iterator.hasNext()){
            MSTNode<T> next = iterator.next();
            containedNodes.add(next);
        }
        MST<T> newSubGraph = new MST<T>(hct);
        for(MSTNode<T> node : containedNodes){
            newSubGraph.add(node.getValue());
        }
        return newSubGraph;
    }

    private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> getMST(){
        if(graph.vertexSet().size() == 1) return graph; // PrimMinimum

        MinimumSpanningTree<MSTNode<T>, DefaultWeightedEdge> internMst = new KruskalMinimumSpanningTree<>(graph);
        Set<DefaultWeightedEdge> edges = internMst.getMinimumSpanningTreeEdgeSet();
        Set<MSTNode<T>> nodes = graph.vertexSet();
        SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> internSWG = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for(MSTNode<T> node : nodes) internSWG.addVertex(node);
        for(DefaultWeightedEdge dwe : edges){
            DefaultWeightedEdge newEdge = internSWG.addEdge(graph.getEdgeSource(dwe), graph.getEdgeTarget(dwe));
            internSWG.setEdgeWeight(newEdge, graph.getEdgeWeight(dwe));
        }
        return internSWG;
    }

    public String toString(){
        try {
            return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(), graph.edgeSet().size(), nucleus.getValue());
        } catch (Exception e){
            return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(), graph.edgeSet().size(), "###Error while getting the nucleus" + e.getMessage());
        }

    }

}
