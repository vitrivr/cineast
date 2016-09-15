package org.vitrivr.cineast;

import com.jujutsu.tsne.MemOptimizedTSne;
import com.jujutsu.tsne.TSne;
import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.PlotPanel;
import org.math.plot.plots.ScatterPlot;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.explorative.HCT;


import javax.swing.*;
import java.util.*;

/**
 * Created by silvanstich on 08.09.16.
 */



public class SilvanPlayground {

    static int initialDimension = 0;
    public static void main(String[] args){

        try {
            HCT<Double> hct = new HCT<Double>((List<List<Double>> arguments) -> {
                List<Double> firstVector = arguments.get(0);
                List<Double> secondVector = arguments.get(1);
                Double distance = 0.0;
                for (int i = 0; i < firstVector.size(); i++) {
                    distance += (firstVector.get(i) - secondVector.get(i)) * (firstVector.get(i) - secondVector.get(i));
                }
                return Math.sqrt(distance);
            });
            hct.insert(Arrays.asList(new Double(1), new Double(-4)), 0);
            hct.insert(Arrays.asList(new Double(1), new Double(-1)), 0);
            hct.insert(Arrays.asList(new Double(1), new Double(1)), 0);
            hct.insert(Arrays.asList(new Double(1), new Double(3)), 0);
            hct.insert(Arrays.asList(new Double(1), new Double(-5)), 0);

            System.out.println("Finished");

        } catch (Exception e) {
            e.printStackTrace();
        }

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


//class HCT{
//    HCTNode root;
//    int topLevelNo = 0;
//    HashMap<Integer, List<HCTNode>> levels = new HashMap<Integer, List<HCTNode>>();
//    public void add(Object nextItem){
//        if(root == null){
//            root = new HCTNode(nextItem);
//            topLevelNo++;
//        } else {
//            add(nextItem, 0);
//        }
//
//    }
//
//    public void add(Object nextItem, int levelNo){
//
//        if(levelNo > topLevelNo){
//            topLevelNo++;
//            HCTNode tcell = new HCTNode(nextItem);
//            tcell.addItem(nextItem);
//            // to be discussed
//            root = tcell;
//            return;
//        }
//
//        HCTNode cellO;
//        if(levelNo == topLevelNo){
//            cellO = root;
//        } else {
//            List<HCTNode> ArrayCS = new ArrayList<HCTNode>();
//            ArrayCS.add(root);
//            cellO = PreEmptiveCellSearch(ArrayCS, nextItem, topLevelNo, levelNo);
//        }
//        Object oldNucleus = cellO.getNucleus();
//        List<HCTNode> newNodes = cellO.addItem(nextItem);
//        int levelAbove = levelNo + 1;
//        if(newNodes.size() == 2){
//
//            remove(levelAbove, oldNucleus);
//            add(newNodes.get(0).getNucleus(), levelAbove);
//            add(newNodes.get(1).getNucleus(), levelAbove);
//        }   else if(oldNucleus != cellO.getNucleus()){
//            remove(levelAbove, oldNucleus);
//            add(cellO.getNucleus(), levelAbove);
//        }
//    }
//
//    private HCTNode PreEmptiveCellSearch(List<HCTNode> ArrayCS, Object nextItem, int currLevelNo, int levelNo){
//        HCTNode msNode = getMostSimilarItem(nextItem, ArrayCS); // // TODO: 12.09.16 find the most similar item in ArrayCS
//        if(levelNo + 1 == currLevelNo){
//            return msNode;
//        }
//        List<HCTNode> newArrayCS = new ArrayList<>();
//        double delta_i = 0;
//        for(HCTNode cell : ArrayCS){
//            try {
//                delta_i = distance(((Nucleus)nextItem).value, cell.getNucleus().value); // // TODO: 12.09.16 implement the covering radius
//            } catch (Exception e){
//                System.out.println(e.getMessage());
//                System.exit(-1);
//            }
//
//            if(delta_i < 10){ // // TODO: 12.09.16 d_min needs  to be implemented
//                newArrayCS.add(cell);
//            }
//        }
//        return PreEmptiveCellSearch(newArrayCS, nextItem, currLevelNo - 1, levelNo);
//    }
//
//    private HCTNode getMostSimilarItem(Object item, List<HCTNode> toCompareItems){
//        double minDist = Double.MAX_VALUE;
//        HCTNode mostSimilarItem = null;
//        for(HCTNode item2 : toCompareItems){
//            try {
//                double actDist = distance(item, item2.getNucleus().value);
//                if(minDist > actDist){
//                    minDist = actDist;
//                    mostSimilarItem = item2;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return mostSimilarItem;
//    }
//
//    public double distance(Object item, Object item2) throws Exception {
//        //// TODO: 12.09.16 implement real distance function
//        double dist = 0;
//        if(item instanceof double[] && item2 instanceof double[]){
//
//            double[] itemCasted = (double[]) item; double[] item2Casted = (double[]) item2;
//            for(int i = 0; i < ((double[]) item).length; i++){
//                dist += itemCasted[i] * itemCasted[i] + item2Casted[i] * item2Casted[i];
//            }
//        } else throw new Exception("Current implementation works only on double[]!");
//        return dist;
//
//    }
//
//    public void remove(int levelNo, Object item){
//        HCTNode containingNode = searchValue(root, item);
//        containingNode.removeItem(item);
//    }
//
//    public HCTNode searchValue(HCTNode parent, Object value){
//        HCTNode _return = null;
//        for(Object nodesInGraph : parent.getValue()){
//            if(nodesInGraph instanceof Nucleus){
//                HCTNode child = ((Nucleus) nodesInGraph).hctNode;
//                if(Arrays.asList(child.getValue()).contains(value)){
//                    return child;
//                } else {
//                    _return = searchValue(child, value);
//                }
//            }
//
//        }
//        return _return;
//    }
//}
//
//class HCTNode{
//
//    private GraphInCell data = new GraphInCell(this);
//
//    private HCTNode(){}
//
//    public HCTNode(Object value){
//        if( value instanceof double[]){
//            data.addNode((double[]) value);
//        }
//
//    }
//
//    public HCTNode(GraphInCell g) {
//        this.data = g;
//    }
//
//    public Object[] getValue(){
//        return data.getData();
//    }
//
//    public List<HCTNode> addItem(Object value){
//
//        List<HCTNode> _return = new ArrayList<>();
//        for(GraphInCell g : data.addNode((double[]) value)){
//            _return.add(new HCTNode(g));
//        }
//        return _return;
//    }
//
//    public void removeItem(Object item){
//        data.removeData(item);
//    }
//
//    public Nucleus getNucleus(){
//        return data.getNucleus();
//    }
//
//    public HCTNode getChild(Object item){
//        double minDist = Double.MAX_VALUE;
//        HCTNode _return = null;
//        for(Object value : getValue()){
//            try {
//                if(value instanceof Nucleus){
//                    Nucleus nucleus = (Nucleus) value;
//                    double actDist = GraphInCell.distance(nucleus.value, item);
//                    if(actDist < minDist){
//                        minDist = actDist;
//                        _return = nucleus.hctNode;
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return _return;
//    }
//}
//
//class GraphInCell{
//
//    private SimpleWeightedGraph graph = new SimpleWeightedGraph(DefaultWeightedEdge.class);
//    Nucleus nucleus;
//    HCTNode hctNode;
//
//    public GraphInCell(HCTNode hctNode){
//        this.hctNode = hctNode;
//    }
//
//    private GraphInCell(SimpleWeightedGraph g, Nucleus nucleus, HCTNode hctNode){
//        this.graph = g;
//        this.nucleus = nucleus;
//        this.hctNode = hctNode;
//    }
//
//    public void removeData(Object item){
//        graph.removeVertex(item);
//    }
//
//    public Nucleus getNucleus(){ return nucleus; }
//
//    public Object[] getData(){
//        return graph.vertexSet().toArray();
//    }
//
//    public List<GraphInCell> addNode(double[] value){
//        List<GraphInCell> _return = new ArrayList<>();
//        Set<Object> vertices = insertVertex(value);
//        SimpleWeightedGraph mst = null;
//        if(graph.vertexSet().size() == 1){
//            nucleus = new Nucleus(graph.vertexSet().toArray()[0], hctNode); // if the graph contains only one node, this must be the nucleus
//        } else {
//            mst = defineNewNucleusIfNecessary(vertices);
//        }
//        if(graph.vertexSet().size() > 1){
//            _return = mitosis(mst);
//        }
//        return _return;
//    }
//
//    private Set<Object> insertVertex(Object value) {
//        try {
//            graph.addVertex(value);
//            Set<Object> vertices = graph.vertexSet();
//            for(Object vertex : vertices){ // // TODO: 13.09.16 creating a fully connected graph and then build a mst from this graph might be not the most efficient way to solve this problem
//                for(Object innerVertex : vertices){
//                    if(vertex != innerVertex && !(graph.containsEdge(vertex, innerVertex) || graph.containsEdge(innerVertex, vertex))){
//                        graph.setEdgeWeight( graph.addEdge(vertex, innerVertex), distance(vertex, innerVertex));
//                    }
//                }
//            }
//            return vertices;
//        } catch (Exception e){
//            e.getMessage();
//            System.exit(-1);
//        }
//        return null;
//    }
//
//    private SimpleWeightedGraph defineNewNucleusIfNecessary(Set<Object> vertices) {
//        MinimumSpanningTree mst = new PrimMinimumSpanningTree(graph);
//        SimpleWeightedGraph g = new SimpleWeightedGraph(DefaultWeightedEdge.class);
//        for(Object vertex : vertices){
//            g.addVertex(vertex);
//        }
//        Set<DefaultWeightedEdge> edges = mst.getMinimumSpanningTreeEdgeSet();
//        Iterator<DefaultWeightedEdge> iterator = edges.iterator();
//        while (iterator.hasNext()){
//            DefaultWeightedEdge e = iterator.next();
//            DefaultWeightedEdge newEdge = (DefaultWeightedEdge) g.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
//            g.setEdgeWeight(newEdge, graph.getEdgeWeight(e));
//
//        }
//        Object nucleus = findNucleus(g);
//        return g;
//    }
//
//    private Nucleus findNucleus(SimpleWeightedGraph g) {
//        int maxDegree = 0;
//        Nucleus nucleus = this.nucleus;
//        for(Object vertex : g.vertexSet().toArray()){
//            int deg = g.degreeOf(vertex);
//            if(deg > maxDegree){
//                maxDegree = deg;
//                nucleus.value = vertex;
//            }
//        }
//        return nucleus;
//    }
//
//    public static double distance(Object item, Object item2) throws Exception {
//        //// TODO: 12.09.16 implement real distance function
//        double dist = 0;
//        if(item instanceof double[] && item2 instanceof double[]){
//
//            double[] itemCasted = (double[]) item; double[] item2Casted = (double[]) item2;
//            for(int i = 0; i < ((double[]) item).length; i++){
//                dist += itemCasted[i] * itemCasted[i] + item2Casted[i] * item2Casted[i];
//            }
//        } else throw new Exception("Current implementation works only on double[]!");
//        return Math.sqrt(dist);
//
//    }
//
//    private List<GraphInCell> mitosis(SimpleWeightedGraph g) {
//        Set<DefaultWeightedEdge> allEdges = g.edgeSet();
//        DefaultWeightedEdge edgeWithLeastWeight = null;
//        Iterator iterator = allEdges.iterator();
//        for(;iterator.hasNext();){
//            DefaultWeightedEdge edge = (DefaultWeightedEdge) iterator.next();
//            if(edgeWithLeastWeight == null || g.getEdgeWeight(edge) < g.getEdgeWeight(edgeWithLeastWeight)){
//                edgeWithLeastWeight = edge;
//            }
//        }
//        g.removeEdge(edgeWithLeastWeight);
//        SimpleWeightedGraph wgOne = getSubGraph(g, g.getEdgeSource(edgeWithLeastWeight));
//        GraphInCell one = new GraphInCell(wgOne, findNucleus(wgOne), hctNode);
//        SimpleWeightedGraph wgTwo = getSubGraph(g, g.getEdgeTarget(edgeWithLeastWeight));
//        GraphInCell two = new GraphInCell(wgTwo, findNucleus(wgTwo), hctNode);
//        return Arrays.asList(one, two);
//    }
//
//    private SimpleWeightedGraph getSubGraph(SimpleWeightedGraph g, Object startVertex) {
//        GraphIterator graphIterator = new BreadthFirstIterator(g, startVertex);
//        List<Object> connectedNodes = new ArrayList<Object>();
//        while(graphIterator.hasNext()){
//            connectedNodes.add(graphIterator.next());
//        }
//        SimpleWeightedGraph wg = new SimpleWeightedGraph(DefaultWeightedEdge.class);
//        Iterator<Object> connectedNodesIterator = connectedNodes.iterator();
//        while(connectedNodesIterator.hasNext()){
//            Object node = connectedNodesIterator.next();
//            wg.addVertex(node);
//            for(Object vertex : wg.vertexSet().toArray()){
//                if(vertex != node) wg.addEdge(vertex, node);
//            }
//        }
//        return wg;
//    }
//
//    // g needs to be a mst and the algorithm returns a new mst with node inserted
//    private void addNodeToMst(SimpleWeightedGraph g, Object z) throws Exception { // TODO: 13.09.16 not finished
//
//        g.addVertex(z);
//        if(g.vertexSet().size() == 1) return;
//        NeighborIndex neighborIndex = new NeighborIndex(g);
//        DefaultWeightedEdge t = null;
//        HashSet<Object> oldNodes = new HashSet<Object>();
//        Object r = g.vertexSet().toArray()[0];
//        List<DefaultWeightedEdge> et = new ArrayList<>();
//
//        oldNodes.add(r);
//        DefaultWeightedEdge dwe = (DefaultWeightedEdge) g.addEdge(r,z);
//        g.setEdgeWeight(dwe, distance(r,z));
//        DefaultWeightedEdge m = null;
//        for(Object w : neighborIndex.neighborListOf(r)){
//            if(t == null) t = (DefaultWeightedEdge) g.getEdge(w, z);
//            if(!oldNodes.contains(w)){
//                addNodeToMst(g, w);
//                double edgeweight = g.getEdgeWeight(t);
//                DefaultWeightedEdge edgeWR = (DefaultWeightedEdge) g.getEdge(w, r);
//                DefaultWeightedEdge k = edgeweight > g.getEdgeWeight(edgeWR) ? t : edgeWR;
//                DefaultWeightedEdge h = edgeweight < g.getEdgeWeight(edgeWR) ? t : edgeWR;
//                et.add(h);
//                if(g.getEdgeWeight(k) < g.getEdgeWeight(m)){
//                    m = k;
//                } else {
//                    g.addEdge(r, z);
//                    m = (DefaultWeightedEdge) g.getEdge(r,z);
//                }
//            }
//        }
//        t = m;
//    }
//}
//
//class Nucleus{
//    Object value;
//    HCTNode hctNode;
//
//    private Nucleus(){}
//
//    public Nucleus(Object value, HCTNode hctNode){
//        this.value = value;
//        this.hctNode = hctNode;
//    }
//}