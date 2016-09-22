package org.vitrivr.cineast.explorative;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by silvanstich on 14.09.16.
 */
public class HCTLevel<T> {

    private List<HCTCell<T>> cells = new ArrayList<>();

    // calculate dmin on this level
    public double dmin(List<T> other, List<HCTCell<T>> arrayCS){
        double dmin = Double.MAX_VALUE;
//        for(HCTCell<T> cell : cells){
        for(HCTCell<T> cell : cells){ // search only in selected cells
            if(cell.isCellDeath() || !arrayCS.contains(cell)) continue;
            if(cell.getDistanceToNucleus(other) < dmin){
                dmin = cell.getDistanceToNucleus(other);
            }
        }
        return dmin;
    }

    public List<HCTCell<T>> getCells(){ return cells;}

    public List<HCTCell<T>> getAllCandidates(List<T> other, double dmin, List<HCTCell<T>> parents){
        List<HCTCell<T>> candidates = new ArrayList<>();
        for(HCTCell<T> cell : cells){
            if(cell.isCellDeath()) continue;
            double distanceToNucleus = cell.getDistanceToNucleus(other);
            double coveringRadius = cell.getCoveringRadius();

            if(distanceToNucleus - coveringRadius < dmin){
                candidates.add(cell);
            }
        }
        return candidates;
    }

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

    public void addCell(List<T> cells){
        cells.addAll(cells);
    }

    public String toString(){
        return String.format("HCTLevel | #cells: %s", cells.size());
    }
}
