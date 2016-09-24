package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HCTLevel<T> implements Serializable {

    private List<HCTCell<T>> cells = new ArrayList<>();

    public List<HCTCell<T>> getCells(){ return cells;}

    public void removeCell(HCTCell<T> cell){
        cells.remove(cell);
    }

    public HCTCell<T> addCell(Mathematics mathematics){
        HCTCell<T> cell = new HCTCell<T>(mathematics);
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
