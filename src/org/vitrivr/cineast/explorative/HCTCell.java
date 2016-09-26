package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by silvanstich on 13.09.16.
 */
public class HCTCell<T extends Comparable<T> & DistanceCalculation<T>> implements IHCTCell<T>, Serializable {

    private static Logger logger = LogManager.getLogger();

    private IMST<T> mst;
    private HCTCell<T> parent;
    private List<HCTCell<T>> children = new ArrayList<>();

    HCTCell() {
        mst = new MST<>();
    }

    private HCTCell(IMST<T> mst, HCTCell<T> parent) {
        this.mst = new MST<>();
        this.mst = mst;
        this.parent = parent;
    }

    void addValue(T value){
        mst.add(value);
    }

    void removeValue(T value){
        mst.remove(value);
    }

    double getDistanceToNucleus(T other) throws Exception{
        return mst.getNucleus().distance(other);
    }

    double getCoveringRadius() throws Exception {
        return mst.getCoveringRadius();
    }

    HCTCell<T> getParent() {
        return parent;
    }

    void setParent(HCTCell<T> parent) {
        this.parent = parent;
    }

    public List<HCTCell<T>> getChildren(){
        return children;
    }

    boolean isReadyForMitosis() { return mst.isReadyForMitosis(); }

    List<HCTCell<T>> mitosis() throws Exception {
        List<MST<T>> msts = mst.mitosis();
        List<HCTCell<T>> newCells = new ArrayList<>();
        for (MST<T> mst : msts) {
            HCTCell<T> newCell = new HCTCell<T>(mst, parent);
            newCells.add(newCell);
            if(parent != null) parent.addChild(newCell);

        }
        for (HCTCell<T> child : children) {
            for (HCTCell<T> newCell : newCells) {
                if(newCell.getValues().contains(child.getNucleus().getValue())){
                    newCell.addChild(child);
                    child.setParent(newCell);
                    break;
                }
            }
        }
        return newCells;

    }

    public MSTNode<T> getNucleus() throws Exception{ return mst.getNucleus(); }

    @Override
    public void addChild(HCTCell child) {
        if(!children.contains(child)) {
            children.add(child);
        }
    }

    @Override
    public boolean containsValue(T value) {
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
        try {
            return String.format("HCTCell | isCellDeath: %s | isReadyMitosis: %s | Nucleus: <%s>",
                    isCellDeath(), isReadyForMitosis(), getNucleus());
        } catch (Exception e){
            return String.format("HCTCell | isCellDeath: %s | isReadyMitosis: %s | Nucleus: <%s>",
                    isCellDeath(), isReadyForMitosis(), "###Error while getting the nucleus! " + e.getMessage());
        }

    }

    public List<T> getValues() {
        return mst.getValues();
    }

}
