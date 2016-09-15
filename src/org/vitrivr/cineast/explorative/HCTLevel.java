package org.vitrivr.cineast.explorative;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by silvanstich on 14.09.16.
 */
public class HCTLevel<T> {

    private List<HCTCell<T>> cells = new ArrayList<>();

    // calculate dmin on this level
    public double dmin(List<T> other){
        double dmin = Double.MAX_VALUE;
        for(HCTCell<T> cell : cells){
            if(cell.getDistanceToNucleus(other) < dmin){
                dmin = cell.getDistanceToNucleus(other);
            }
        }
        return dmin;
    }

    public List<HCTCell<T>> getCells(){ return cells;}

    public List<HCTCell<T>> getAllCandidates(List<T> other, double dminUpperLevel, List<HCTCell<T>> parents){
        List<HCTCell<T>> candidates = new ArrayList<>();
        for(HCTCell<T> cell : cells){
            if(!parents.contains(cell.getParent())) continue; // search only in children of matched nodes one level above
            double distanceToNucleus = cell.getDistanceToNucleus(other);
            double coveringRadius = cell.getCoveringRadius();

            if(distanceToNucleus - coveringRadius < dminUpperLevel){
                candidates.add(cell);
            }
        }
        return candidates;
    }

    public HCTCell<T> addCell(Function<List<List<T>>, Double> distanceCalculation){
        HCTCell<T> cell = new HCTCell<T>(distanceCalculation);
        cells.add(cell);
        return cell;
    }

    public String toString(){
        return String.format("HCTLevel | #cells: %s | cells: %s", cells.size(), Utils.listToString(cells));
    }
}
