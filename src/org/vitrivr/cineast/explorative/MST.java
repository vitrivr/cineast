package org.vitrivr.cineast.explorative;

import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;


public class MST<V extends Number> implements IMST<V> {

    private SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    private Class<MSTNode> concreteMSTNodeClass;

    public MST(Class<MSTNode> MSTNodeClass) {
        this.concreteMSTNodeClass = MSTNodeClass;
    }

    @Override
    public void add(List<V> item) {
//        MSTNode<V> mstNode = new MSTNode<V>(item, this);
        MSTNode<V> mstNode = newMstNode(item, this);
        graph.addVertex(mstNode);
        for (MSTNode<V> node : graph.vertexSet()) {
            if (node != mstNode){
                DefaultWeightedEdge dwe = graph.addEdge(mstNode, node);
                graph.setEdgeWeight(dwe, mstNode.distance(node));
            }
        }
    }

    @Override
    public void remove(List<V> item) {
        for (MSTNode<V> current : graph.vertexSet()) {
            if (current.getValue() == item) {
                graph.removeVertex(current);
            }
        }
    }

    public List<V> getNucleus(){
        SimpleWeightedGraph<MSTNode<V>, DefaultWeightedEdge> mst = getMST();
        int degree = 0;
        MSTNode<V> nucleus = null;
        for (MSTNode<V> current : mst.vertexSet()) {
            if (degree < mst.degreeOf(current)) {
                degree = mst.degreeOf(current);
                nucleus = current;
            }
        }
        return nucleus.getValue();
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

    private MSTNode newMstNode(Object item, Object mst){
        try {
            return concreteMSTNodeClass.getDeclaredConstructor(List.class, MST.class).newInstance(item, mst);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
