package org.vitrivr.cineast.explorative;

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

    public HCT(Function<List<List<T>>, Double> distanceCalculation) {
        this.distanceCalculation = distanceCalculation;
    }

    public void insert(List<T> nextItem){
        System.out.println(print());
        if(!sanityCheck()) System.out.println("### ERROR Child-Relationsship broken");
        System.out.println("Next item to insert is " + Utils.listToString(nextItem));
        System.out.println(System.lineSeparator());
        insert(nextItem, 0);
    }

    private void insert(List<T> nextItem, int levelNo) {
        if (levels.size() == 0){ // first insert
            createInitialRoot(nextItem);
            return;
        }

        int topLevelNo = levels.size() - 1;
        List<HCTCell<T>> topLevelCells = levels.get(levels.size() - 1).getCells();
        HCTCell<T> cellt = topLevelCells.get(0); // get root, normally only one node in topLevel exists

        if(levelNo > topLevelNo){
            createNewRoot(nextItem, topLevelCells);
            return;
        }


        HCTCell<T> cellO;
        if(levelNo == topLevelNo){
            cellO = cellt;
        }else{
            List<HCTCell<T>> arrayCS = new ArrayList<>();
            arrayCS.add(cellt);
            cellO = preemptiveCellSearch(arrayCS, nextItem, topLevelNo, levelNo);
        }
        List<T> oldNucleusValue = cellO.getNucleus().getValue();
        cellO.addValue(nextItem);

        if(cellO.isReadyForMitosis()){
            doMitosis(levelNo, cellO, oldNucleusValue);
        }
        else if(oldNucleusValue != cellO.getNucleus().getValue()){
            nucleusChanged(levelNo, cellO, oldNucleusValue);
        }
    }

    private void nucleusChanged(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        // nucleus change in the root does not have any influence -> ignore! this is important
        if(levelNo == levels.size() - 1) return;
        remove(oldNucleusValue, levelNo + 1);
        insert(cellO.getNucleus().getValue(), levelNo + 1);
    }

    private void doMitosis(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        List<HCTCell<T>> newCells = cellO.mitosis();
        removeOldCell(levelNo, cellO, oldNucleusValue);
        addNewCells(levelNo, newCells);
    }

    private void addNewCells(int levelNo, List<HCTCell<T>> newCells) {
        for (HCTCell<T> newCell : newCells) {
            levels.get(levelNo).addCell(newCell);
        }
        for (HCTCell<T> newCell : newCells) {
            insert(newCell.getNucleus().getValue(), levelNo + 1);
            makeRelations(levelNo, newCell);
        }
    }

    private void removeOldCell(int levelNo, HCTCell<T> cellO, List<T> oldNucleusValue) {
        HCTCell<T> parentCell = cellO.getParent();
        if(parentCell != null) parentCell.removeChild(cellO);
        levels.get(levelNo).removeCell(cellO);
        remove(oldNucleusValue, levelNo + 1);
    }

    private void makeRelations(int levelNo, HCTCell<T> newCell) {
        if(levelNo - 1 >= 0){
            for(HCTCell<T> lowerCell : levels.get(levelNo - 1).getCells()){
                if(lowerCell.isCellDeath()) continue;
                if(newCell.getValues().contains(lowerCell.getNucleus().getValue())){
                    lowerCell.setParent(newCell);
                    newCell.addChild(lowerCell);
                }
            }
        } else {
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
        HCTCell<T> topLevelCell = level.addCell(distanceCalculation); //aka root
        topLevelCell.addValue(nextItem);
        for (HCTCell<T> oldTopLevelCell : levels.get(levels.size() - 2).getCells()) { // the root has all cells in level rootlevel - 1 as its children and those children all have the root as parent
            oldTopLevelCell.setParent(topLevelCell);
            topLevelCell.addChild(oldTopLevelCell);
        }
        return;
    }

    private void createInitialRoot(List<T> nextItem) {
        HCTLevel<T> level = new HCTLevel<>();
        levels.add(level);
        HCTCell<T> cell = level.addCell(distanceCalculation);
        cell.addValue(nextItem);
        return;
    }

    @Override
    public HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, List<T> nextItem, int curLevelNo, int levelNo) { // TODO: 14.09.16 needs lot of testing
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
                        levels.remove(levels.get(topLevelNo));
                    } else{
                        remove(oldNucleusValue, levelNo + 1);
                    }
                }
                else if(cell.isReadyForMitosis()){
                    doMitosis(levelNo, cell, oldNucleusValue);
//                    List<HCTCell<T>> newCells = cell.mitosis();
//                    if(levelNo < topLevelNo) remove(cell.getNucleus().getValue(), levelNo + 1);
//                    for (HCTCell<T> newCell : newCells) {
//                        insert(newCell.getNucleus().getValue(), levelNo + 1);
//                    }
                }
                else if(oldNucleusValue != cell.getNucleus().getValue()){
                    nucleusChanged(levelNo, cell, oldNucleusValue);
//                    if(levelNo < topLevelNo) remove(oldNucleusValue, levelNo + 1);
//                    if(levelNo < topLevelNo) insert(cell.getNucleus().getValue(), levelNo + 1);

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

    private boolean sanityCheck(){
        for(HCTLevel<T> level : levels){
            if(levels.indexOf(level) == levels.size() - 1) return true;
            int sumOfChilds = 0;
            for(HCTCell<T> cell : levels.get(levels.indexOf(level) + 1).getCells()){
                sumOfChilds += cell.getChildren().size();
            }
            if(level.getCells().size() != sumOfChilds) {
                return false;
            }
        }
        return true;
    }
}
