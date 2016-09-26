package org.vitrivr.cineast.explorative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HCTLevel<T extends Comparable<T> &DistanceCalculation<T>> implements Serializable {

    private List<HCTCell<T>> cells = new ArrayList<>();

    public List<HCTCell<T>> getCells(){ return cells;}

    public void removeCell(HCTCell<T> cell){
        cells.remove(cell);
    }

    public HCTCell<T> addCell(){
        HCTCell<T> cell = new HCTCell<T>();
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
