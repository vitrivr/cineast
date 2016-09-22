package org.vitrivr.cineast.explorative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Iterator;
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
        logger.debug(print());
        sanityCheck();
        logger.debug("Next item to insert is " + Utils.listToString(nextItem));
        insert(nextItem, 0);
        size++;
        logger.info("#Items in tree: " + size + " #cells in tree " + getNbrOfCellsInTree() + " #levels in tree: " + (levels.size()));
    }

    private void insert(List<T> nextItem, int levelNo) {
        if (levels.size() == 0){ // first insert
            createInitialRoot(nextItem);
            return;
        }

        List<HCTCell<T>> topLevelCells = getTopLevelCells();

        HCTCell<T> cellT = topLevelCells.get(0); // get root, normally only one node in topLevel exists

        int topLevelNo = levels.size() - 1;
        if(levelNo > topLevelNo){
            createNewRoot(nextItem, topLevelCells);
            return;
        }

        HCTCell<T> cellO = searchCellToInsertNewValue(nextItem, cellT, topLevelNo, levelNo);
        if (cellO == null) return;

        addValue(nextItem, levelNo, cellO);
    }

    private void addValue(List<T> nextItem, int levelNo, HCTCell<T> cellO) {
        List<T> oldNucleusValue = cellO.getNucleus().getValue();
        cellO.addValue(nextItem);
        logger.debug("Added " + Utils.listToString(nextItem) + " to " + cellO);

        if(cellO.isReadyForMitosis()){
            logger.debug("Cell is ready for mitosis: " + cellO);
            doMitosis(levelNo, cellO, oldNucleusValue);
        }
        else if(oldNucleusValue != cellO.getNucleus().getValue()){
            nucleusChanged(levelNo, cellO, oldNucleusValue);
        }
    }

    private List<HCTCell<T>> getTopLevelCells() {
        while(levels.get(levels.size()-1).getCells().size() == 0){
            levels.remove(levels.get(levels.size() - 1));
        }
        List<HCTCell<T>> topLevelCells = levels.get(levels.size() - 1).getCells();
        logger.debug("TopLevelCells: " + Utils.listToString(topLevelCells));
        if(levels.size() > 1){
            logger.debug("First level: " + Utils.listToString(levels.get(levels.size() - 2).getCells()));
            if(levels.size() > 2) {
                logger.debug("Second level: " + Utils.listToString(levels.get(levels.size() - 3).getCells()));
            }
        }
        return topLevelCells;
    }

    private HCTCell<T> searchCellToInsertNewValue(List<T> nextItem, HCTCell<T> cellt, int topLevelNo, int levelNo) {
        HCTCell<T> cellO;
        if(levelNo == topLevelNo){
            cellO = cellt;
        }else{
            List<HCTCell<T>> arrayCS = new ArrayList<>();
            arrayCS.add(cellt);
            cellO = preemptiveCellSearch(arrayCS, nextItem, topLevelNo, levelNo);
        }

        if(cellO == null){
            logger.debug("Empty cell. Created new cell to fix.");
            cellO = levels.get(levelNo).addCell(distanceCalculation, comperatorFunction, compactnessFunction);
            cellO.addValue(nextItem);
            makeRelations(levelNo, cellO);
            return null;
        }
        return cellO;
    }

    private void nucleusChanged(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        // nucleus change in the root does not have any influence -> ignore! this is important
        if(levelNo == levels.size() - 1) return;
        logger.debug("Nucleus changing...");
        remove(oldNucleusValue, levelNo + 1);
        cellO.getParent().removeChild(cellO);
        insert(cellO.getNucleus().getValue(), levelNo + 1);
        makeRelations(levelNo, cellO);
    }

    private void doMitosis(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        logger.debug("Mitosis...");
        List<HCTCell<T>> newCells = cellO.mitosis();
        logger.debug("New cells are " + Utils.listToString(newCells));
        removeOldCell(levelNo, cellO, oldNucleusValue);
        addNewCells(levelNo, newCells);
    }

    private void addNewCells(int levelNo, List<HCTCell<T>> newCells) {
        logger.debug("Adding new cells. Cells " + Utils.listToString(newCells));
        for (HCTCell<T> newCell : newCells) {
            levels.get(levelNo).addCell(newCell);
        }
        for (HCTCell<T> newCell : newCells) {
            insert(newCell.getNucleus().getValue(), levelNo + 1);
            makeRelations(levelNo, newCell);
        }
    }

    private void removeOldCell(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        logger.debug("Removing old cell. Old cell: " + cellO);
        HCTCell<T> parentCell = cellO.getParent();
        if(parentCell != null) parentCell.removeChild(cellO);
        levels.get(levelNo).removeCell(cellO);
        remove(oldNucleusValue, levelNo + 1);
    }

    private void makeRelations(int levelNo, HCTCell<T> newCell) {
        logger.debug("Creating relations...");
        if(levelNo - 1 >= 0){
            for(HCTCell<T> lowerCell : levels.get(levelNo - 1).getCells()){
                if(lowerCell.isCellDeath()) continue;
                if(newCell.getValues().contains(lowerCell.getNucleus().getValue())){
                    lowerCell.setParent(newCell);
                    newCell.addChild(lowerCell);
                }
            }
        }
        if(levelNo + 1 < levels.size()){
            for(HCTCell<T> upperCell : levels.get(levelNo + 1).getCells()){
                if(upperCell.getValues().contains(newCell.getNucleus().getValue())){
                    upperCell.addChild(newCell);
                    newCell.setParent(upperCell);
                }
            }
        }
    }

    private void createNewRoot(List<T> nextItem, List<HCTCell<T>> topLevelCells) {
        HCTLevel<T> level = new HCTLevel<T>();
        levels.add(level);
        HCTCell<T> topLevelCell = level.addCell(distanceCalculation, comperatorFunction, compactnessFunction); //aka root
        topLevelCell.addValue(nextItem);
        for (HCTCell<T> oldTopLevelCell : levels.get(levels.size() - 2).getCells()) { // the root has all cells in level rootlevel - 1 as its children and those children all have the root as parent
            oldTopLevelCell.setParent(topLevelCell);
            topLevelCell.addChild(oldTopLevelCell);
        }
        logger.debug("new Root created. Root is: " + topLevelCell);
        return;
    }

    private void createInitialRoot(List<T> nextItem) {
        HCTLevel<T> level = new HCTLevel<>();
        levels.add(level);
        HCTCell<T> cell = level.addCell(distanceCalculation, comperatorFunction, compactnessFunction);
        cell.addValue(nextItem);
        logger.debug("Initial root created!");
        return;
    }

    @Override
    public HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, List<T> nextItem, int curLevelNo, int levelNo) {
        double dmin = levels.get(curLevelNo).dmin(nextItem, ArrayCS); // dmin of parent level
        if(curLevelNo == levelNo + 1){
            return getMSCell(ArrayCS, nextItem, curLevelNo);
        }
        List<HCTCell<T>> newArrayCS = getAllCandidates(nextItem, dmin, ArrayCS);
        return preemptiveCellSearch(newArrayCS, nextItem, curLevelNo - 1, levelNo);
    }

    private HCTCell<T> getMSCell(List<HCTCell<T>> ArrayCS, List<T> nextItem, int currentLevel){
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
    public void remove(List<T> value, int levelNo) {
        logger.debug("Removing value. value: " + Utils.listToString(value));
        int topLevelNo = levels.size() - 1;
        List<HCTCell<T>> cells = levels.get(levels.size() - 1).getCells();
        if(cells.size() == 0 || levelNo > topLevelNo) return; // experimental
        HCTCell<T> cellT = cells.get(0); // get root

        Iterator<HCTCell<T>> iterator = levels.get(levelNo).getCells().iterator();

        while (iterator.hasNext()) {
            HCTCell<T> cell = iterator.next();
            if (cell.containsValue(value)){
                List<T> oldNucleusValue = cell.getNucleus().getValue();
                cell.removeValue(value);
                if(cell.isCellDeath()){
                    if(levelNo == topLevelNo){
                        logger.debug("Top cell is cell-death");
                        levels.remove(levels.get(topLevelNo));
                        logger.debug("New top level is " + Utils.listToString(levels.get(levels.size() - 1).getCells()));
                    } else{
                        logger.debug("cell-death occured");
                        cell.getParent().removeChild(cell);
                        cell.setParent(null);
                        iterator.remove();
                        remove(oldNucleusValue, levelNo + 1);
                    }
                }
                else if(cell.isReadyForMitosis()){
                    logger.debug("Cell is ready for mitosis after remove.");
                    doMitosis(levelNo, cell, oldNucleusValue);
                }
                else if(oldNucleusValue != cell.getNucleus().getValue()){
                    logger.debug("Nucleus changed after remove.");
                    nucleusChanged(levelNo, cell, oldNucleusValue);
                }
            }
        }
    }

    public String toString(){
        return String.format("HCT | #levels: %s", levels.size());
    }

    public String print(){

        StringBuilder sb = new StringBuilder();
        for(HCTLevel<T> level : levels){
            sb.append("level : " ).append(levels.indexOf(level)).append(" ");
            for(HCTCell<T> cell : level.getCells()){
                sb.append(Utils.listToString(cell.getValues())).append(" | Parent (Nucleus): ").append(cell.getNucleus().getValue());
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

    private List<HCTCell<T>> getAllCandidates(List<T> other, double dmin, List<HCTCell<T>> parents){
        List<HCTCell<T>> candidates = new ArrayList<>();
        for(HCTCell<T> parent : parents){
            for(HCTCell<T> cell : parent.getChildren()){
                if(cell.isCellDeath()) continue;
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
}
