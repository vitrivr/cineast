package org.vitrivr.cineast.explorative;

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

    public HCT(Function<List<List<T>>, Double> distanceCalculation) {
        this.distanceCalculation = distanceCalculation;
    }

    @Override
    public void insert(List<T> nextItem, int levelNo) {
        if (levels.size() == 0){ // first insert / root insert
            HCTLevel level = new HCTLevel();
            levels.add(level);
            HCTCell<T> cell = level.addCell(distanceCalculation);
            cell.addValue(nextItem);
            return;
        }
        int topLevelNo = levels.size() - 1;
        List<HCTCell<T>> topLevelCells = levels.get(levels.size() - 1).getCells();
        HCTCell<T> cellt = topLevelCells.get(0); // get root, normally only one node in topLevel exists
        if(levelNo > topLevelNo){ // create new root
            HCTLevel<T> level = new HCTLevel<T>();
            levels.add(level);
            HCTCell<T> topLevelCell = level.addCell(distanceCalculation); //aka root
            topLevelCell.addValue(nextItem);
            for (HCTCell<T> oldTopLevelCell : topLevelCells) {
                oldTopLevelCell.setParent(topLevelCell);
                topLevelCell.setChild(oldTopLevelCell);
            }

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

        //experimental
//        if(levels.size() > 1){ // obviously can not set child and parent if only one level exists
//            HCTCell<T> mSCell =  getMSCell(levels.get(levelNo - 1).getCells(), nextItem, levelNo);
//            mSCell.setParent(cellO);
//            cellO.addChild(mSCell);
//        }



        if(cellO.isReadyForMitosis()){
            List<HCTCell<T>> newCells = cellO.mitosis();
            HCTCell<T> parentCell = cellO.getParent();
            if(parentCell != null) parentCell.removeChild(cellO);
            levels.get(levelNo).removeCell(cellO);
            remove(oldNucleusValue, levelNo + 1);
            for (HCTCell<T> newCell : newCells) {
                levels.get(levelNo).addCell(newCell);
            }
            for (HCTCell<T> newCell : newCells) {
                insert(newCell.getNucleus().getValue(), levelNo + 1);
            }
        }
        else if(oldNucleusValue != cellO.getNucleus().getValue()){
            remove(oldNucleusValue, levelNo + 1);
            insert(cellO.getNucleus().getValue(), levelNo + 1);
        }
    }

    @Override
    public HCTCell<T> preemptiveCellSearch(List<HCTCell<T>> ArrayCS, List<T> nextItem, int curLevelNo, int levelNo) { // TODO: 14.09.16 needs lot of testing
//        if(levels.size() == 1) return levels.get(1).getCells().get(1); // if only one layer exists, there is only the root, so return that
        double dmin = levels.get(curLevelNo).dmin(nextItem);
        //HCTCell<T> mSCell = getMSCell(ArrayCS, nextItem, curLevelNo);
        if(curLevelNo == levelNo + 1){
            List<HCTCell<T>> children = getMSCell(ArrayCS, nextItem, curLevelNo).getChildren();
            double dist = Double.MAX_VALUE;
            HCTCell<T> closest = null;
            for (HCTCell<T> child : children) {
                if (dist > child.getDistanceToNucleus(nextItem)){
                    dist = child.getDistanceToNucleus(nextItem);
                    closest = child;
                }
            }
            return closest;
        }
        List<HCTCell<T>> newArrayCS = levels.get(curLevelNo).getAllCandidates(nextItem, dmin, ArrayCS);
        return preemptiveCellSearch(newArrayCS, nextItem, curLevelNo, levelNo);
    }

    @Override
    public void remove(List<T> value, int levelNo) {
        int topLevelNo = levels.size() - 1;
        List<HCTCell<T>> cells = levels.get(levels.size() - 1).getCells();
        if(cells.size() == 0) return; // experimental
        HCTCell<T> cellT = cells.get(0); // get root

        for (HCTCell<T> cell : levels.get(levelNo).getCells()) {
            if (cell.containsValue(value)){
                List<T> oldNucleusValue = cell.getNucleus().getValue();
                cell.removeValue(value);
                if(cell.isCellDeath()){
                    if(levelNo == topLevelNo){
                        levels.remove(levels.get(topLevelNo));
                    } else{
                        List<T> nucleusValue = cell.getNucleus().getValue();
                    }
                }
                else if(cell.isReadyForMitosis()){
                    List<HCTCell<T>> newCells = cell.mitosis();
                    if(levelNo < topLevelNo) remove(cell.getNucleus().getValue(), levelNo + 1);
                    for (HCTCell<T> newCell : newCells) {
                        insert(newCell.getNucleus().getValue(), levelNo + 1);
                    }
                }
                else if(oldNucleusValue != cell.getNucleus().getValue()){
                    if(levelNo < topLevelNo) remove(oldNucleusValue, levelNo + 1);
                    // experimental
                    if(levelNo < topLevelNo) insert(cell.getNucleus().getValue(), levelNo + 1);

                }
            }
        }


    }

    private HCTCell<T> getMSCell(List<HCTCell<T>> ArrayCS, List<T> nextItem, int currentLevel){
        HCTCell<T> mSCell = null;
        List<HCTCell<T>> cells = levels.get(currentLevel).getCells();
        double dist = Double.MAX_VALUE;
        for (HCTCell<T> cell : cells) {
            if(ArrayCS.contains(cell.getParent()) || cell.getParent() == null){
                if(cell.getDistanceToNucleus(nextItem) < dist){
                    dist = cell.getDistanceToNucleus(nextItem);
                    mSCell = cell;
                }
            }
        }
        return mSCell;
    }

    public String toString(){
        return String.format("HCT | #levels: %s", levels.size());
    }
}
