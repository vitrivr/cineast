package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by silvanstich on 13.09.16.
 */
public class HCT<T> implements IHCT<T>{

    // first element in list is top level, last element is ground level
    private List<HCTLevel<T>> levels = new ArrayList<>();
    private Function<List<List<T>>, Double> distanceCalculation;
    private Function<List<List<T>>, Double> comperatorFunction;
    private static Logger logger = LogManager.getLogger();
    private Function<SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge>, Double> compactnessFunction;
    private int size;


    /**
     * Initializes a new HCT
     * @param distanceCalculation A function which calculates the distance between two elements. The two arguments are given as a list with two entries.
     * @param comperatorFunction A function that compares two elements. Return <0 if the first item is bigger, 0 if the items are equal and 0> if the first item is smaller.
     * @param compactnessFunction A function that determines the compactness of the cell. Return >0.5 if the compactness is to low and the cell should split, return <0.5 if the compactness is suffiecent.
     */
    public HCT(Function<List<List<T>>, Double> distanceCalculation, Function<List<List<T>>, Double> comperatorFunction, Function<SimpleWeightedGraph<MSTNode<T>, DefaultWeightedEdge>, Double> compactnessFunction) {
        this.distanceCalculation = distanceCalculation;
        this.comperatorFunction = comperatorFunction;
        this.compactnessFunction = compactnessFunction;
    }

    public void insert(List<T> nextItem) throws Exception {
        sanityCheck();
        insert(nextItem, 0);
        logger.info("#Items in tree: " + ++size + " #cells in tree " + getNbrOfCellsInTree() + " #levels in tree: " + (levels.size()));
    }

    private HCTCell<T> insert(List<T> nextItem, int levelNo) throws Exception {
        if (levels.size() == 0){ // first insert
            createInitialRoot(nextItem);
            return null;
        }

        List<HCTCell<T>> topLevelCells = getTopLevelCells();

        HCTCell<T> cellT = topLevelCells.get(0); // get root, normally only one node in topLevel exists

        int topLevelNo = levels.size() - 1;
        if(levelNo > topLevelNo){
            return createNewRoot(nextItem, topLevelCells);
        }

        HCTCell<T> cellO = searchCellToInsertNewValue(nextItem, cellT, topLevelNo, levelNo);

        cellO = addValue(nextItem, levelNo, cellO);

        return cellO;
    }

    private HCTCell<T> addValue(List<T> nextItem, int levelNo, HCTCell<T> cellO) throws Exception {
        List<T> oldNucleusValue = cellO.getNucleus().getValue();
        cellO.addValue(nextItem);

        if(cellO.isReadyForMitosis()){
            List<HCTCell<T>> newCells = doMitosis(levelNo, cellO, oldNucleusValue);
            for (HCTCell<T> newCell : newCells) {
                if(newCell.getValues().contains(nextItem)) {
                    return newCell;
                }
            }
            throw new Exception("Can not find cellO after mitosis!");
        }
        else if(oldNucleusValue != cellO.getNucleus().getValue()){
            nucleusChanged(levelNo, cellO, oldNucleusValue);
        }
        return cellO;
    }

    private List<HCTCell<T>> getTopLevelCells() {
        while(levels.get(levels.size()-1).getCells().size() == 0){
            levels.remove(levels.get(levels.size() - 1));
        }
        List<HCTCell<T>> topLevelCells = levels.get(levels.size() - 1).getCells();
        return topLevelCells;
    }

    private HCTCell<T> searchCellToInsertNewValue(List<T> nextItem, HCTCell<T> cellt, int topLevelNo, int levelNo) throws Exception {
        HCTCell<T> cellO;
        if(levelNo == topLevelNo){
            cellO = cellt;
        }else{
            List<HCTCell<T>> arrayCS = new ArrayList<>();
            arrayCS.add(cellt);
            cellO = preemptiveCellSearch(arrayCS, nextItem, topLevelNo, levelNo);
        }
        if(cellO == null) throw new Exception("###ERROR No cell found!");
        return cellO;
    }

    private void nucleusChanged(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) throws Exception {
        // nucleus change in the root does not have any influence -> ignore! this is important
        if(levelNo == levels.size() - 1) return;
        remove(cellO, oldNucleusValue, levelNo + 1);
        cellO.getParent().removeChild(cellO);
        HCTCell<T> newParent = insert(cellO.getNucleus().getValue(), levelNo + 1);
        cellO.setParent(newParent);
        newParent.addChild(cellO);
    }

    private List<HCTCell<T>> doMitosis(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) throws Exception {
        List<HCTCell<T>> newCells = cellO.mitosis();
        removeOldCell(levelNo, cellO, oldNucleusValue);
        addNewCells(levelNo, newCells);
        return newCells;
    }

    private void addNewCells(int levelNo, List<HCTCell<T>> newCells) throws Exception {
        for (HCTCell<T> newCell : newCells) {
            levels.get(levelNo).addCell(newCell);
        }
        for (HCTCell<T> newCell : newCells) {
            HCTCell<T> parentCell = insert(newCell.getNucleus().getValue(), levelNo + 1);
            newCell.getParent().removeChild(newCell);
            parentCell.addChild(newCell);
            newCell.setParent(parentCell);
        }
    }

    private void removeOldCell(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) throws Exception {
        HCTCell<T> parentCell = cellO.getParent();
        if(parentCell != null) parentCell.removeChild(cellO);
        levels.get(levelNo).removeCell(cellO);
        remove(cellO, oldNucleusValue, levelNo + 1);
    }

    private HCTCell<T> createNewRoot(List<T> nextItem, List<HCTCell<T>> topLevelCells) {
        HCTLevel<T> level = new HCTLevel<T>();
        levels.add(level);
        HCTCell<T> topLevelCell = level.addCell(distanceCalculation, comperatorFunction, compactnessFunction); //aka root
        topLevelCell.addValue(nextItem);
        for (HCTCell<T> oldTopLevelCell : levels.get(levels.size() - 2).getCells()) { // the root has all cells in level rootlevel - 1 as its children and those children all have the root as parent
            oldTopLevelCell.setParent(topLevelCell);
            topLevelCell.addChild(oldTopLevelCell);
        }
        return topLevelCell;
    }

    private void createInitialRoot(List<T> nextItem) {
        HCTLevel<T> level = new HCTLevel<>();
        levels.add(level);
        HCTCell<T> cell = level.addCell(distanceCalculation, comperatorFunction, compactnessFunction);
        cell.addValue(nextItem);
        return;
    }

    @Override
    public HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, List<T> nextItem, int curLevelNo, int levelNo) throws Exception {
        double dmin = dmin(nextItem, ArrayCS); // dmin of parent level
        if(curLevelNo == levelNo + 1){
            return getMSCell(ArrayCS, nextItem);
        }
        List<HCTCell<T>> newArrayCS = getAllCandidates(nextItem, dmin, ArrayCS);
        return preemptiveCellSearch(newArrayCS, nextItem, curLevelNo - 1, levelNo);
    }

    private HCTCell<T> getMSCell(List<HCTCell<T>> ArrayCS, List<T> nextItem) throws Exception {
        HCTCell<T> mSCell = null;
        double dist = Double.MAX_VALUE;

        for (HCTCell<T> parent : ArrayCS) {
            for (HCTCell<T> cell : parent.getChildren()) {
                if(cell.isCellDeath()) continue;
                if(cell.getDistanceToNucleus(nextItem) < dist){
                    dist = cell.getDistanceToNucleus(nextItem);
                    mSCell = cell;
                }
            }
        }
        return mSCell;
    }

    @Override
    public void remove(HCTCell<T> cellO, List<T> value, int levelNo) throws Exception {
        int topLevelNo = levels.size() - 1;
        List<HCTCell<T>> cells = levels.get(levels.size() - 1).getCells();

        HCTCell<T> parentCell = cellO.getParent();
        if(cells.size() == 0 || levelNo > topLevelNo) return; // experimental
        if(!parentCell.containsValue(value)) throw new Exception("Parent cell does not contain expectedd nucleus! Child cell: " + cellO);

        List<T> oldNucleusValue = parentCell.getNucleus().getValue();
        parentCell.removeValue(value);
        if(parentCell.isCellDeath()){
            if(levelNo == topLevelNo){
                levels.remove(levels.get(topLevelNo));
            } else{
                parentCell.getParent().removeChild(parentCell);
                levels.get(levelNo).getCells().remove(parentCell);
                remove(parentCell, oldNucleusValue, levelNo + 1);
            }
        } else if(parentCell.isReadyForMitosis()){
            doMitosis(levelNo, parentCell, oldNucleusValue);
        }
        else if(oldNucleusValue != parentCell.getNucleus().getValue()){
            nucleusChanged(levelNo, parentCell, oldNucleusValue);
        }
    }

    public String toString(){
        return String.format("HCT | #levels: %s", levels.size());
    }

    private String print() throws Exception {

        StringBuilder sb = new StringBuilder();
        for(HCTLevel<T> level : levels){
            sb.append("level : " ).append(levels.indexOf(level)).append(" ");
            for(HCTCell<T> cell : level.getCells()){
                sb.append(Utils.listToString(cell.getValues())).append(" | Parent (Nucleus): ");
                sb.append(cell.getNucleus().getValue());
                sb.append("......");
            }
            sb.append(System.lineSeparator());
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("{empty tree}");
        }
        return sb.toString();
    }

    private List<HCTCell<T>> getAllCandidates(List<T> other, double dmin, List<HCTCell<T>> parents) throws Exception {
        List<HCTCell<T>> candidates = new ArrayList<>();
        for(HCTCell<T> parent : parents){
            for(HCTCell<T> cell : parent.getChildren()){
                double distanceToNucleus = cell.getDistanceToNucleus(other);
                double coveringRadius = cell.getCoveringRadius();

                if(distanceToNucleus - coveringRadius <= dmin){
                    candidates.add(cell);
                }
            }
        }

        return candidates;
    }

    private void sanityCheck() throws Exception{
        for(HCTLevel<T> level : levels){
            if(levels.indexOf(level) == levels.size() - 1) return;
            int sumOfChildren = 0;
            for(HCTCell<T> cell : levels.get(levels.indexOf(level) + 1).getCells()){
                sumOfChildren += cell.getChildren().size();
            }
            if(level.getCells().size() != sumOfChildren) {
                throw new Exception("### ERROR child-relationship is broken. level: " + levels.indexOf(level) + ", #cells on this level: " + level.getCells().size() + ", #children on the upper level:" + sumOfChildren);
            }
        }
    }

    public List<HCTCell<T>> getGroundLevelCells(){
        return levels.get(0).getCells();
    }

    public HCTCell<T> getRoot() throws Exception {
        if(levels.get(levels.size() - 1).getCells().size() != 1) throw new Exception("Root is ambiguous! # of cells on top level is " + levels.get(levels.size() - 1).getCells().size());
        return levels.get(levels.size() - 1).getCells().get(0);
    }

    public long getNbrOfCellsInTree(){
        long nbrOfCells = 0;
        for(HCTLevel<T> level : levels){
            nbrOfCells += level.getCells().size();
        }
        return nbrOfCells;
    }

    private double dmin(List<T> other, List<HCTCell<T>> arrayCS) throws Exception {
        double dmin = Double.MAX_VALUE;
        for(HCTCell<T> cell : arrayCS){ // search only in selected cells
            if(cell.getDistanceToNucleus(other) < dmin){
                dmin = cell.getDistanceToNucleus(other);
            }
        }
        return dmin;
    }
}
