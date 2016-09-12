package org.vitrivr.cineast;

import com.jujutsu.tsne.MemOptimizedTSne;
import com.jujutsu.tsne.TSne;
import org.jgraph.graph.Edge;
import org.jgraph.plaf.basic.BasicGraphUI;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.plots.ScatterPlot;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import sun.misc.resources.Messages_zh_CN;

import javax.swing.*;
import java.util.*;

/**
 * Created by silvanstich on 08.09.16.
 */



public class SilvanPlayground {

    static int initialDimension = 0;
    public static void main(String[] args){
        GraphInCell g = new GraphInCell();
        g.addNode(new double[]{10});
        g.addNode(new double[]{8});
        g.addNode(new double[]{5.0});
        g.addNode(new double[]{11});
        g.addNode(new double[]{6});
        g.addNode(new double[]{4});


//
//        DBSelector dbSelector = Config.getDatabaseConfig().getSelectorSupplier().get();
//        dbSelector.open("cineast_representativeframes");
//        List<Map<String, PrimitiveTypeProvider>> l = dbSelector.getRows("id", "65539");
//        Integer frame = 0;
//        if (l.size() > 0) {
//            for(Map<String, PrimitiveTypeProvider> m : l){
//                PrimitiveTypeProvider ptp = m.get("frame");
//                frame = ptp.getInt();
//                System.out.println(frame);
//            }
//        }

    }

    private static double[][] tsneSelf(double[][] x, int perplexity, int numIter, int learningRate, int monumentum){
        return null;

    }

    private static double findSimga(int perplexity, double[][] x, int i, double sigma_i){
        double perp = calcPerplexity(x, i, sigma_i);
        double oldPerp = perp;
        while(perp -1 < perplexity || perp+1 > perplexity){
            double diff = perp - oldPerp;
            perp = calcPerplexity(x, i, sigma_i);
            sigma_i = perp - oldPerp > diff ? sigma_i - 0.1 : sigma_i + 0.1;
        }
        return 0;
    }

    private static double calcPerplexity(double[][] x, int i, double sigma_i) {
        double entropy = 0;
        for(int j = 0; j < x.length; j++){
            double pji = propInHighDim(x, j, i, sigma_i);
            entropy += - pji*Math.log(pji)/Math.log(2);
        }
        return Math.pow(2, entropy);
    }

    private static double propInhighDim(double[][] x, int i, int j, int perplexity){
        if(i == j) return 0;
        double[] xi = x[i]; double[] xj = x[j];
        double euclDistanceXiXj = euclideanDistance(xi, xj);
        double sigma_i = findSimga(perplexity, x, i, 1.0);
        return 0;
    }

    private static double propInHighDim(double[][] x, int i, int j, double sigma_i){
        if(i == j) return 0;
        double[] xi = x[i]; double[] xj = x[j];
        double euclDistanceXiXj = euclideanDistance(xi, xj);
        double numerator = Math.exp(-(euclDistanceXiXj*euclDistanceXiXj) / (2*sigma_i*sigma_i));
        double divisor = 1;
        for(int k = 0; k < x.length; k++){
            if(k==i) continue;
            double euclDistanceXiXk = euclideanDistance(xi, x[k]);
            divisor +=  Math.exp(-(euclDistanceXiXk*euclDistanceXiXk) / (2*sigma_i*sigma_i));
        }
        return numerator / divisor;
    }

    private static double propInLowDim(double[][] y, int i, int j){
        if(i == j) return 0;
        double[] yi = y[i]; double[] yj = y[j];
        double euclDistanceYiYj = euclideanDistance(yi, yj);
        double numerator = Math.exp(-(euclDistanceYiYj*euclDistanceYiYj));
        double divisor = 1;
        for(int k = 0; k < y.length; k++){
            if(k==i) continue;
            double euclDistanceYiYk = euclideanDistance(yi, y[k]);
            divisor +=  Math.exp(-(euclDistanceYiYk*euclDistanceYiYk));
        }
        return numerator / divisor;
    }

    private static double euclideanDistance(double[] xi, double[] xj){
        double distance = 0;
        for(int i = 0; i < xi.length; i++){
            distance += xi[i] * xi[i] + xj[i] * xj[i];
        }
        return distance;
    }

    private static void tsne() {
        System.out.println("Max heap size is: " + Runtime.getRuntime().maxMemory());
        DBSelector dbSelector1 = Config.getDatabaseConfig().getSelectorSupplier().get();
        dbSelector1.open("features_averagecolor");
        List<PrimitiveTypeProvider> list = dbSelector1.getAllRows("feature");
        dbSelector1.close();
        System.out.println(list.size());

        initialDimension = list.get(0).getFloatArray().length;
        double[][] initalArray = new double[list.size()][initialDimension];

        int i = 0;
        for(PrimitiveTypeProvider ptp : list){
            double[] featureVektor = convertToDoubleArray(ptp.getFloatArray());
            initalArray[i] = featureVektor;
            i++;
        }

        TSne tSne = new MemOptimizedTSne();
        double[][] result = tSne.tsne(initalArray, 2, initialDimension, 20, 1000, true);
        plotIris(result);
    }

    static void plotIris(double[][] Y) {
        double [][]        setosa = new double[initialDimension][2];
        String []     setosaNames = new String[initialDimension];
        double [][]    versicolor = new double[initialDimension][2];
        String [] versicolorNames = new String[initialDimension];
        double [][]     virginica = new double[initialDimension][2];
        String []  virginicaNames = new String[initialDimension];

        int cnt = 0;
        for (int i = 0; i < initialDimension; i++, cnt++) {
            for (int j = 0; j < 2; j++) {
                setosa[i][j] = Y[cnt][j];
                setosaNames[i] = "setosa";
            }
        }
        for (int i = 0; i < initialDimension; i++, cnt++) {
            for (int j = 0; j < 2; j++) {
                versicolor[i][j] = Y[cnt][j];
                versicolorNames[i] = "versicolor";
            }
        }
        for (int i = 0; i < initialDimension; i++, cnt++) {
            for (int j = 0; j < 2; j++) {
                virginica[i][j] = Y[cnt][j];
                virginicaNames[i] = "virginica";
            }
        }

        Plot2DPanel plot = new Plot2DPanel();

        ScatterPlot setosaPlot = new ScatterPlot("setosa", PlotPanel.COLORLIST[0], setosa);
        setosaPlot.setTags(setosaNames);

        ScatterPlot versicolorPlot = new ScatterPlot("versicolor", PlotPanel.COLORLIST[1], versicolor);
        versicolorPlot.setTags(versicolorNames);
        ScatterPlot virginicaPlot = new ScatterPlot("versicolor", PlotPanel.COLORLIST[2], virginica);
        virginicaPlot.setTags(virginicaNames);

        plot.plotCanvas.setNotable(true);
        plot.plotCanvas.setNoteCoords(true);
        plot.plotCanvas.addPlot(setosaPlot);
        plot.plotCanvas.addPlot(versicolorPlot);
        plot.plotCanvas.addPlot(virginicaPlot);

        //int setosaId = plot.addScatterPlot("setosa", setosa);
        //int versicolorId = plot.addScatterPlot("versicolor", versicolor);
        //int virginicaId = plot.addScatterPlot("virginica", virginica);

        FrameView plotframe = new FrameView(plot);
        plotframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        plotframe.setVisible(true);
    }

    private static double[] convertToDoubleArray(float[] floats){
        double[] doubles = new double[floats.length];
        for(int i=0; i<floats.length; i++){
            doubles[i] = floats[i];
        }
        return doubles;
    }
}


class HCT{
    HCTNode root;
    int topLevelNo = 0;

    public void add(Object nextItem, int levelNo){
        if(levelNo > topLevelNo){
            topLevelNo += 1;
            HCTNode tcell = new HCTNode(nextItem);
            tcell.addItem(nextItem);
            // to be discussed
            root = tcell;
            return;
        }
        HCTNode cellO;
        if(levelNo == topLevelNo){
            cellO = root;
        } else {
            List<HCTNode> ArrayCS = new ArrayList<HCTNode>();
            ArrayCS.add(root);
            cellO = PreEmptiveCellSearch(ArrayCS, nextItem, topLevelNo, levelNo);
        }
        Object oldNucleus = cellO.nucleus;
        List<Object> newNnuclei = cellO.addItem(nextItem);
        int levelAbove = levelNo + 1;
        if(newNnuclei.size() == 2){

            remove(oldNucleus, levelAbove);
            add(newNnuclei.get(0), levelAbove);
            add(newNnuclei.get(1), levelAbove);
        }   else if(oldNucleus != cellO.nucleus){
            remove(oldNucleus, levelAbove);
            add(cellO.nucleus, levelAbove);
        }
    }

    private HCTNode PreEmptiveCellSearch(List<HCTNode> ArrayCS, Object nextItem, int currLevelNo, int levelNo){
        HCTNode msNode = null; // // TODO: 12.09.16 find the most similar item in ArrayCS
        if(levelNo + 1 == currLevelNo){
            return msNode;
        }
        List<HCTNode> newArrayCS = new ArrayList<>();
        double delta_i = 0;
        for(HCTNode cell : ArrayCS){
            try {
                delta_i = distance(nextItem, cell.nucleus); // // TODO: 12.09.16 implement the covering radius
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.exit(-1);
            }

            if(delta_i < 10){ // // TODO: 12.09.16 d_min needs  to be implemented
                newArrayCS.add(cell);
            }
        }
        return PreEmptiveCellSearch(newArrayCS, nextItem, currLevelNo - 1, levelNo);
    }

    public double distance(Object item, Object item2) throws Exception {
        //// TODO: 12.09.16 implement real distance function
        double dist = 0;
        if(item instanceof double[] && item2 instanceof double[]){

            double[] itemCasted = (double[]) item; double[] item2Casted = (double[]) item2;
            for(int i = 0; i < ((double[]) item).length; i++){
                dist += itemCasted[i] * itemCasted[i] + item2Casted[i] * item2Casted[i];
            }
        } else throw new Exception("Current implementation works only on double[]!");
        return dist;

    }

    public HCTNode remove(Object item, int levelNo){
        return null;
    }
}

class HCTNode{
    private List<HCTNode> childCells;
    private WeightedGraph graph = new SimpleWeightedGraph(DefaultWeightedEdge.class);

    public Object nucleus;
    public boolean nucleusIsChanged = false;

    private HCTNode() {};

    public HCTNode(Object value){
        addValueToGraph(value);
        nucleus = value;
    }

    public HCTNode(WeightedGraph g, Object nucleus) {
        this.nucleus = nucleus;
        this.graph = g;
    }

    private void addValueToGraph(Object value) {

    }

    public Object getValue(){
        return null;
    }

    public List<Object> addItem(Object value){
        addValueToGraph(value);
        return null;
    }

}

class GraphInCell{

    SimpleWeightedGraph graph = new SimpleWeightedGraph(DefaultWeightedEdge.class);
    Object nucleus;

    public void addNode(double[] value){
        Set<Object> vertices = insertVertex(value);
        SimpleWeightedGraph mst = null;
        if(graph.vertexSet().size() == 1){
            nucleus = graph.vertexSet().toArray()[0]; // if the graph contains only one node, this must be the nucleus
        } else {
            mst = defineNewNucleusIfNecessary(vertices);
        }
        if(graph.vertexSet().size() > 5){
            mitosis(mst);
        }
    }

    private Set<Object> insertVertex(Object value) {
        graph.addVertex(value);
        Set<Object> vertices = graph.vertexSet();
        for(Object vertex : vertices){
            if(vertex != value){
                try {
                    DefaultWeightedEdge dwe = (DefaultWeightedEdge) graph.addEdge(value, vertex);
                    graph.setEdgeWeight(dwe, distance(value, vertex));
                } catch (Exception e) {
                    e.getMessage();
                    System.exit(-1);
                }
            }
        }
        return vertices;
    }

    private SimpleWeightedGraph defineNewNucleusIfNecessary(Set<Object> vertices) {
        MinimumSpanningTree mst = new PrimMinimumSpanningTree(graph);
        SimpleWeightedGraph g = new SimpleWeightedGraph(DefaultWeightedEdge.class);
        for(Object vertex : vertices){
            g.addVertex(vertex);
        }
        Set<DefaultWeightedEdge> edges = mst.getMinimumSpanningTreeEdgeSet();
        Iterator<DefaultWeightedEdge> iterator = edges.iterator();
        while (iterator.hasNext()){
            DefaultWeightedEdge e = iterator.next();
            DefaultWeightedEdge newEdge = (DefaultWeightedEdge) g.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
            g.setEdgeWeight(newEdge, graph.getEdgeWeight(e));

        }
        Object nucleus = findNucleus(g);
        return g;
    }

    private Object findNucleus(SimpleWeightedGraph g) {
        int maxDegree = 0;
        Object nucleus = this.nucleus;
        for(Object vertex : g.vertexSet().toArray()){
            int deg = g.degreeOf(vertex);
            if(deg > maxDegree){
                maxDegree = deg;
                nucleus = vertex;
            }
        }
        return nucleus;
    }

    private double distance(Object item, Object item2) throws Exception {
        //// TODO: 12.09.16 implement real distance function
        double dist = 0;
        if(item instanceof double[] && item2 instanceof double[]){

            double[] itemCasted = (double[]) item; double[] item2Casted = (double[]) item2;
            for(int i = 0; i < ((double[]) item).length; i++){
                dist += itemCasted[i] * itemCasted[i] + item2Casted[i] * item2Casted[i];
            }
        } else throw new Exception("Current implementation works only on double[]!");
        return Math.sqrt(dist);

    }

    private void mitosis(SimpleWeightedGraph g) {
        Set<DefaultWeightedEdge> allEdges = g.edgeSet();
        DefaultWeightedEdge edgeWithLeastWeight = null;
        Iterator iterator = allEdges.iterator();
        for(;iterator.hasNext();){
            DefaultWeightedEdge edge = (DefaultWeightedEdge) iterator.next();
            if(edgeWithLeastWeight == null || g.getEdgeWeight(edge) < g.getEdgeWeight(edgeWithLeastWeight)){
                edgeWithLeastWeight = edge;
            }
        }
        g.removeEdge(edgeWithLeastWeight);
        SimpleWeightedGraph wgOne = getSubGraph(g, g.getEdgeSource(edgeWithLeastWeight));
        HCTNode one = new HCTNode(wgOne, findNucleus(wgOne));
        SimpleWeightedGraph wgTwo = getSubGraph(g, g.getEdgeTarget(edgeWithLeastWeight));
        HCTNode two = new HCTNode(wgTwo, findNucleus(wgTwo));
    }

    private SimpleWeightedGraph getSubGraph(SimpleWeightedGraph g, Object startVertex) {
        GraphIterator graphIterator = new BreadthFirstIterator(g, startVertex);
        List<Object> connectedNodes = new ArrayList<Object>();
        while(graphIterator.hasNext()){
            connectedNodes.add(graphIterator.next());
        }
        SimpleWeightedGraph wg = new SimpleWeightedGraph(DefaultWeightedEdge.class);
        Iterator<Object> connectedNodesIterator = connectedNodes.iterator();
        while(connectedNodesIterator.hasNext()){
            Object node = connectedNodesIterator.next();
            wg.addVertex(node);
            for(Object vertex : wg.vertexSet().toArray()){
                if(vertex != node) wg.addEdge(vertex, node);
            }
        }
        return wg;
    }
}