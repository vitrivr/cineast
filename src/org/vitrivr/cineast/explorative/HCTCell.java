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
    private Function<List<List<T>>, Double> comperatorFunction;

    public HCTCell(Function<List<List<T>>, Double> distanceCalculation, Function<List<List<T>>, Double> comperatorFunction) {
        this.distanceCalculation = distanceCalculation;
        this.comperatorFunction = comperatorFunction;
        mst = new MST<>(this.distanceCalculation, comperatorFunction);
    }

    public HCTCell(Function<List<List<T>>, Double> distanceCalculation, IMST<T> mst, HCTCell<T> parent, Function<List<List<T>>, Double> comperatorFunction) {
        this.distanceCalculation = distanceCalculation;
        this.comperatorFunction = comperatorFunction;
        this.mst = new MST<>(this.distanceCalculation, comperatorFunction);
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
        System.out.println("New parent is set. Parent: " + parent + " this: " + this);
        this.parent = parent;
    }

    public List<HCTCell<T>> getChildren(){
        return children;
    }

    public boolean isReadyForMitosis() { return mst.isReadyForMitosis(); }

    public List<HCTCell<T>> mitosis() {
        List<MST<T>> msts = mst.mitosis();
        List<HCTCell<T>> newCells = new ArrayList<>();
        for (MST<T> mst : msts) {
            newCells.add(new HCTCell<T>(distanceCalculation, mst, parent, comperatorFunction));
        }
        return newCells;

    }

    public MSTNode<T> getNucleus(){ return mst.getNucleus(); }

    @Override
    public void addChild(HCTCell child) {
        if(!children.contains(child)) {
            System.out.println("New child is added. Child: " + child + " this: " + this);
            children.add(child);
        } else {
            System.out.println("Child is already in child list. Child: " + child + "this: " + this);
        }
    }

    @Override
    public boolean containsValue(List value) {
        return mst.containsValue(value);
    }

    @Override
    public boolean isCellDeath() {
        return mst.isCellDeath();
    }

    @Override
    public void removeChild(HCTCell child) {
        children.remove(child);
    }

    public String toString(){
        return String.format("HCTCell | isCellDeath: %s | isReadyMitosis: %s | Nucleus: <%s>",
                isCellDeath(), isReadyForMitosis(), getNucleus());
    }

    public List<List<T>> getValues() {
        return mst.getValues();
    }

    public HCTCell<T> getChildByNucleus(List<T> nucleus){
        for(HCTCell<T> ChildCell : children){
            if(ChildCell.getNucleus().getValue() == nucleus) return ChildCell;
        }
        return null;
    }

    public HCTCell<T> getChildByContainingValue(List<T> value){
        for(HCTCell<T> childCell : children){
            if(childCell.getValues().contains(value)) return childCell;
        }
        return null;
    }
}
