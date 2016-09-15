package org.vitrivr.cineast.explorative;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public class HCTCell<T> implements IHCTCell {

    private Function<List<List<T>>, Double> distanceCalculation;
    private IMST<T> mst;
    private HCTCell<T> parent;
    private List<HCTCell<T>> children = new ArrayList<>();

    public HCTCell(Function<List<List<T>>, Double> distanceCalculation) {
        this.distanceCalculation = distanceCalculation;
        mst = new MST<>(this.distanceCalculation);
    }

    public HCTCell(Function<List<List<T>>, Double> distanceCalculation, IMST<T> mst, HCTCell<T> parent) {
        this.distanceCalculation = distanceCalculation;
        this.mst = new MST<>(this.distanceCalculation);
        this.mst = mst;
        this.parent = parent;
    }

    public void addValue(List<T> value){
        mst.add(value);
    }

    public void removeValue(List<T> value){
        mst.remove(value);
    }

    public double getDistanceToNucleus(List<T> other){
        return mst.getNucleus().distance(other, distanceCalculation);
    }

    public double getCoveringRadius(){
        return mst.getCoveringRadius();
    }

    public HCTCell<T> getParent() {
        return parent;
    }

    public void setParent(HCTCell<T> parent) {
        this.parent = parent;
    }

    public void addChild(HCTCell<T> child){
        children.add(child);
    }

    public List<HCTCell<T>> getChildren(){
        return children;
    }

    public boolean isReadyForMitosis() { return mst.isReadyForMitosis(); }

    public List<HCTCell<T>> mitosis() {
        List<MST<T>> msts = mst.mitosis();
        List<HCTCell<T>> newCells = new ArrayList<>();
        for (MST<T> mst : msts) {
            newCells.add(new HCTCell<T>(distanceCalculation, mst, parent));
        }
        return newCells;

    }

    public MSTNode<T> getNucleus(){ return mst.getNucleus(); }

    @Override
    public boolean containsValue(List value) {
        return mst.containsValue(value);
    }

    @Override
    public boolean isCellDeath() {
        return mst.isCellDeath();
    }

    public String toString(){
        return String.format("HCTCell | parent: %s | children: %s | mst: %s | isCellDeath: %s | isReadyMitosis: %s | Nucleus: <%s>",
                parent, Utils.listToString(children), mst, isCellDeath(), isReadyForMitosis(), getNucleus());
    }
}
