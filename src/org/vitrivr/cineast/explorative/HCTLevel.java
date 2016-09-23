package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HCTLevel<T> {

    private List<HCTCell<T>> cells = new ArrayList<>();

    public List<HCTCell<T>> getCells(){ return cells;}

    public void removeCell(HCTCell<T> cell){
        cells.remove(cell);
    }

    public HCTCell<T> addCell(Function<List<List<T>>, Double> distanceCalculation, Function<List<List<T>>, Double> comperatorFunction, Function<SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge>, Double> compactnessFunction){
        HCTCell<T> cell = new HCTCell<T>(compactnessFunction, distanceCalculation, comperatorFunction);
        cells.add(cell);
        return cell;
    }

    public void addCell(HCTCell<T> cell){
        cells.add(cell);
    }

    public String toString(){
        return String.format("HCTLevel | #cells: %s", cells.size());
    }
}
