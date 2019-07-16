package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import com.google.common.collect.Iterables;

class MST<T extends Comparable<T>> implements IMST<T>, Serializable {

  private static final long serialVersionUID = -4358572815727537508L;

  private final HCT<T> hct;

  private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(
      DefaultWeightedEdge.class);
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
      if (node != mstNode) {
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
    while (iterator.hasNext()) {
      MSTNode<T> current = iterator.next();
      if (current.getValue() == item) {
        toDeleteNodes.add(current);
      }
    }
    graph.removeAllVertices(toDeleteNodes);
    mst = getMST();
    if (mst.vertexSet().size() > 0) {
      nucleus = updateNucleus();
      coveringRadius = updateCoveringRadius();
    }
  }

  @Override
  public MSTNode<T> getNucleus() {
    return nucleus;
  }

  private MSTNode<T> updateNucleus() throws java.lang.Exception {
    if (graph.vertexSet().isEmpty()) {
      throw new Exception("This graph contains no nodes!");
    }
    if (graph.vertexSet().size() == 1) {
      return Iterables.getOnlyElement(graph.vertexSet());
    }
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

  @Override
  public double getCompactness() {
    return hct.getCompactnessCalculation().getCompactness(graph);
  }

  @Override
  public boolean isReadyForMitosis() {

    return getCompactness() > 0.5; // // TODO: 14.09.16 needs real implemenation
  }

  @Override
  public List<IMST<T>> mitosis() throws Exception {
    double largestWeight = 0;
    DefaultWeightedEdge largestEdge = null;
    List<DefaultWeightedEdge> edges = new ArrayList<>();
    edges.addAll(mst.edgeSet());
    Collections.sort(edges, (el1, el2) -> graph.getEdgeSource(el1).getValue()
        .compareTo(graph.getEdgeSource(el2).getValue()));
    for (DefaultWeightedEdge edge : edges) {
      if (mst.getEdgeWeight(edge) > largestWeight) {
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
      if (mstNode.getValue() == value) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<T> getValues() {
    List<T> values = new ArrayList<>();
    for (MSTNode<T> node : graph.vertexSet()) {
      values.add(node.getValue());
    }
    return values;
  }

  @Override
  public double getCoveringRadius() throws Exception {
    return coveringRadius;
  }

  // covering radius means the distance from the nucleus to the furthest element of the mst
  private double updateCoveringRadius() throws Exception {
    double coveringRadius = 0;
    for (MSTNode<T> node : mst.vertexSet()) {
      double pathLength = DijkstraShortestPath.findPathBetween(mst, nucleus, node).getWeight();
      if (pathLength > coveringRadius) {
        coveringRadius = pathLength;
      }
    }
    return coveringRadius;
  }

  private MST<T> createSubGraph(SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> mst,
      MSTNode<T> startNode) throws Exception {
    List<MSTNode<T>> containedNodes = new ArrayList<>();
    GraphIterator<MSTNode<T>, DefaultWeightedEdge> iterator = new BreadthFirstIterator<>(mst,
        startNode);
    while (iterator.hasNext()) {
      MSTNode<T> next = iterator.next();
      containedNodes.add(next);
    }
    MST<T> newSubGraph = new MST<T>(hct);
    for (MSTNode<T> node : containedNodes) {
      newSubGraph.add(node.getValue());
    }
    return newSubGraph;
  }

  private SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> getMST() {
    if (graph.vertexSet().size() == 1)
     {
      return graph; // PrimMinimum
    }

    SpanningTree<DefaultWeightedEdge> internMst = new KruskalMinimumSpanningTree<>(graph)
        .getSpanningTree();
    Set<DefaultWeightedEdge> edges = internMst.getEdges();
    Set<MSTNode<T>> nodes = graph.vertexSet();
    SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge> internSWG = new SimpleWeightedGraph<>(
        DefaultWeightedEdge.class);
    for (MSTNode<T> node : nodes) {
      internSWG.addVertex(node);
    }
    for (DefaultWeightedEdge dwe : edges) {
      DefaultWeightedEdge newEdge = internSWG.addEdge(graph.getEdgeSource(dwe),
          graph.getEdgeTarget(dwe));
      internSWG.setEdgeWeight(newEdge, graph.getEdgeWeight(dwe));
    }
    return internSWG;
  }

  @Override
  public String toString() {
    try {
      return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(),
          graph.edgeSet().size(), nucleus.getValue());
    } catch (Exception e) {
      return String.format("MST | #nodes: %s | #edges: %s | nucleus: %s ", graph.vertexSet().size(),
          graph.edgeSet().size(), "###Error while getting the nucleus" + e.getMessage());
    }

  }

}
