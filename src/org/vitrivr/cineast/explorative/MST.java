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


class MST<T> implements IMST<T> {

    private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    private Function<List<List<T>>, Double> distanceMetric;
    private Function<List<List<T>>, Double> comperatorFunction;
    private Function<SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge>, Double> compactnessFunction;

    MST(Function<List<List<T>>, Double> distanceMetric, Function<List<List<T>>, Double> comperatorFunction, Function<SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge>, Double> compactnessFunction) {
        this.distanceMetric = distanceMetric;
        this.comperatorFunction = comperatorFunction;
        this.compactnessFunction = compactnessFunction;
    }

    @Override
    public void add(List<T> item) {
        MSTNode<T> mstNode = new MSTNode(item, this);
        graph.addVertex(mstNode);
        for (MSTNode<T> node : graph.vertexSet()) {
            if (node != mstNode){
                DefaultWeightedEdge dwe = graph.addEdge(mstNode, node);
                graph.setEdgeWeight(dwe, mstNode.distance(node, distanceMetric));
            }
        }
    }

    @Override
    public void remove(List<T> item) {
        Iterator<MSTNode<T>> iterator = graph.vertexSet().iterator();
        List<MSTNode<T>> toDeleteNodes = new ArrayList<>();
        while(iterator.hasNext()){
            MSTNode<T> current = iterator.next();
            if(current.getValue() == item){
                toDeleteNodes.add(current);
            }
        }
        graph.removeAllVertices(toDeleteNodes);
    }

    public MSTNode<T> getNucleus() throws java.lang.Exception{
        if(graph.vertexSet().size() == 0) throw new Exception(String.format("This graph contains no nodes!"));
        if(graph.vertexSet().size() == 1){
            return (MSTNode<T>) graph.vertexSet().toArray()[0];}
        SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> mst = getMST();
        int degree = 0;
        MSTNode<T> nucleus = null;
        List<MSTNode<T>> mstNodes = new ArrayList<>();
        mstNodes.addAll(mst.vertexSet());
        Collections.sort(mstNodes, (o1, o2) -> MST.this.compare(o1.getValue(), o2.getValue()));

        for (MSTNode<T> current : mstNodes) {
            if (degree < mst.degreeOf(current)) {
                degree = mst.degreeOf(current);
                nucleus = current;
            }
        }
        return nucleus;
    }

    public double getCompactness(){
        // TODO: 14.09.16 implement real compactness measurement
        return compactnessFunction.apply(getMST());
    }

    public boolean isReadyForMitosis(){

        return getCompactness() > 0.5; // // TODO: 14.09.16 needs real implemenation
    }

    public List<MST<T>> mitosis(){
        SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> mst = getMST();
        double largestWeight = 0;
        DefaultWeightedEdge largestEdge = null;
        List<DefaultWeightedEdge> edges = new ArrayList<>();
        edges.addAll(mst.edgeSet());
        Collections.sort(edges, (el1, el2) -> compare(graph.getEdgeSource(el1).getValue(), graph.getEdgeSource(el2).getValue()));
        for(DefaultWeightedEdge edge : edges){
            if(mst.getEdgeWeight(edge) > largestWeight){
                largestWeight = mst.getEdgeWeight(edge);
                largestEdge = edge;
            }
        }
        List<MST<T>> newGraphs = new ArrayList<>();
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
    public boolean containsValue(List<T> value) { // TODO: 14.09.16 is a bottleneck
        for (MSTNode<T> mstNode : graph.vertexSet()) {
            if(mstNode.getValue() == value) return true;
        }
        return false;
    }

    @Override
    public List<List<T>> getValues() {
        List<List<T>> values = new ArrayList<>();
        for(MSTNode<T> node : graph.vertexSet()){
            values.add(node.getValue());
        }
        return values;
    }

    // covering radius means the distance from the nucleus to the furthest element of the mst
    public double getCoveringRadius() throws Exception{
        SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> mst = getMST();
        MSTNode<T> nucleus = getNucleus();
        double coveringRadius = 0;
        for(MSTNode<T> node : mst.vertexSet()){
            double pathLength = new DijkstraShortestPath(mst, nucleus, node).getPathLength();
            if(pathLength > coveringRadius) coveringRadius = pathLength;
        }
        return coveringRadius;
    }

    private MST<T> createSubGraph(SimpleWeightedGraph mst, MSTNode<T> startNode){
        List<MSTNode<T>> containedNodes = new ArrayList<>();
        GraphIterator<MSTNode<T>, DefaultWeightedEdge> iterator = new BreadthFirstIterator<>(mst, startNode);
        while(iterator.hasNext()){
            MSTNode<T> next = iterator.next();
            containedNodes.add(next);
        }
        MST<T> newSubGraph = new MST<T>(distanceMetric, comperatorFunction, compactnessFunction);
        for(MSTNode<T> node : containedNodes){
            newSubGraph.add(node.getValue());
        }
        return newSubGraph;
    }

    private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> getMST(){
        if(graph.vertexSet().size() == 1) return graph; // PrimMinimum

        MinimumSpanningTree<MSTNode<T>, DefaultWeightedEdge> internMst = new PrimMinimumSpanningTree<>(graph);
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
            return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(), graph.edgeSet().size(), Utils.listToString(getNucleus().getValue()));
        } catch (Exception e){
            return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(), graph.edgeSet().size(), "###Error while getting the nucleus" + e.getMessage());
        }

    }

    private int compare(List<T> one, List<T> two){
        List<List<T>> args = new ArrayList<>();
        args.add(one);
        args.add(two);
        double result = comperatorFunction.apply(args);
        if(result > 0 ) {
            return 1;
        }
        else if(result < 0){
            return -1;
        }
        else{
            return 0;
        }

    }
}
