package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.explorative.TreeTraverserHorizontal;

public class HCT<T extends Comparable<T>> implements IHCT<T>, Serializable {

  private static final long serialVersionUID = -1094933156856445682L;
  // first element in list is top level, last element is ground level
  private List<HCTLevel<T>> levels = new ArrayList<>();
  private static Logger logger = LogManager.getLogger();
  private int size;
  private boolean isReRun = false;

  private final CompactnessCalculation compactnessCalculation;
  private final DistanceCalculation<T> distanceCalculation;

  public HCT(CompactnessCalculation compactnessCalculation,
      DistanceCalculation<T> distanceCalculation) {
    this.compactnessCalculation = compactnessCalculation;
    this.distanceCalculation = distanceCalculation;
  }

  @Override
  public void insert(T nextItem) throws Exception {
    isReRun = false;
    sanityCheck();
    insert(nextItem, 0);
    size++;
    if (size % 1000 == 0) {
      logger.info(System.currentTimeMillis() + "     #Items in tree: " + size + " #cells in tree "
          + getNbrOfCellsInTree() + " #levels in tree: " + (levels.size()));
    }
    logger.debug("#Items in tree: " + size + " #cells in tree " + getNbrOfCellsInTree()
        + " #levels in tree: " + (levels.size()));
  }

  private IHCTCell<T> insert(T nextItem, int levelNo) throws Exception {
    if (levels.isEmpty()) { // first insert
      createInitialRoot(nextItem);
      return null;
    }

    List<HCTCell<T>> topLevelCells = getTopLevelCells();

    HCTCell<T> cellT = topLevelCells.get(0); // get root, normally only one node in topLevel exists

    int topLevelNo = levels.size() - 1;
    if (levelNo > topLevelNo) {
      return createNewRoot(nextItem, topLevelCells);
    }

    IHCTCell<T> cellO = searchCellToInsertNewValue(nextItem, cellT, topLevelNo, levelNo);

    cellO = addValue(nextItem, levelNo, cellO);

    return cellO;
  }

  private IHCTCell<T> addValue(T nextItem, int levelNo, IHCTCell<T> cellO) throws Exception {
    T oldNucleusValue = cellO.getNucleus().getValue();
    cellO.addValue(nextItem);

    if (cellO.isReadyForMitosis()) {
      List<HCTCell<T>> newCells = doMitosis(levelNo, cellO, oldNucleusValue);
      for (HCTCell<T> newCell : newCells) {
        if (newCell.getValues().contains(nextItem)) {
          return newCell;
        }
      }
      throw new Exception("Can not find cellO after mitosis!");
    } else if (oldNucleusValue != cellO.getNucleus().getValue()) {
      nucleusChanged(levelNo, cellO, oldNucleusValue);
    }
    return cellO;
  }

  private List<HCTCell<T>> getTopLevelCells() {
    while (levels.get(levels.size() - 1).getCells().isEmpty()) {
      levels.remove(levels.get(levels.size() - 1));
    }
    return levels.get(levels.size() - 1).getCells();
  }

  private IHCTCell<T> searchCellToInsertNewValue(T nextItem, IHCTCell<T> cellt, int topLevelNo,
      int levelNo) throws Exception {
    IHCTCell<T> cellO;
    if (levelNo == topLevelNo) {
      cellO = cellt;
    } else {
      List<IHCTCell<T>> arrayCS = new ArrayList<>();
      arrayCS.add(cellt);
      cellO = preemptiveCellSearch(arrayCS, nextItem, topLevelNo, levelNo);
    }
    if (cellO == null) {
      throw new Exception("###ERROR No cell found!");
    }
    return cellO;
  }

  private void nucleusChanged(int levelNo, IHCTCell<T> cellO, T oldNucleusValue) throws Exception {
    // nucleus change in the root does not have any influence -> ignore! this is important
    if (levelNo == levels.size() - 1) {
      return;
    }
    remove(cellO, oldNucleusValue, levelNo + 1);
    cellO.getParent().removeChild(cellO);
    IHCTCell<T> newParent = insert(cellO.getNucleus().getValue(), levelNo + 1);
    cellO.setParent(newParent);
    newParent.addChild(cellO);
  }

  private List<HCTCell<T>> doMitosis(int levelNo, IHCTCell<T> cellO, T oldNucleusValue)
      throws Exception {
    List<HCTCell<T>> newCells = cellO.mitosis();
    removeOldCell(levelNo, cellO, oldNucleusValue);
    addNewCells(levelNo, newCells);
    return newCells;
  }

  int counter;

  private void addNewCells(int levelNo, List<HCTCell<T>> newCells) throws Exception {
    List<HCTCell<T>> oneValueCells = new ArrayList<>();
    newCells
        .sort((HCTCell<T> el1, HCTCell<T> el2) -> el1.getValues().size() - el2.getValues().size());
    for (HCTCell<T> newCell : newCells) {
      levels.get(levelNo).addCell(newCell);
    }
    for (HCTCell<T> newCell : newCells) {
      IHCTCell<T> parentCell = insert(newCell.getNucleus().getValue(), levelNo + 1);
      newCell.getParent().removeChild(newCell);
      parentCell.addChild(newCell);
      newCell.setParent(parentCell);
      if (newCell.getValues().size() == 1 || !isReRun || levelNo == 0) {
        oneValueCells.add(newCell);
      }
    }
    for (HCTCell<T> oneValueCell : oneValueCells) {
      if (levelNo == 0 && !isReRun && oneValueCell.getValues().size() == 1) {
        counter++;
        logger.debug("# of reruns is: " + counter);
        isReRun = true;
        removeOldCell(0, oneValueCell, oneValueCell.getNucleus().getValue());
        insert(oneValueCell.getNucleus().getValue(), 0);
      }

    }
  }

  private void removeOldCell(int levelNo, IHCTCell<T> cellO, T oldNucleusValue) throws Exception {
    IHCTCell<T> parentCell = cellO.getParent();
    if (parentCell != null) {
      parentCell.removeChild(cellO);
    }
    levels.get(levelNo).removeCell(cellO);
    remove(cellO, oldNucleusValue, levelNo + 1);
  }

  private HCTCell<T> createNewRoot(T nextItem, List<HCTCell<T>> topLevelCells) throws Exception {
    HCTLevel<T> level = new HCTLevel<>(this);
    levels.add(level);
    HCTCell<T> topLevelCell = level.addCell(); // aka root
    topLevelCell.addValue(nextItem);
    for (HCTCell<T> oldTopLevelCell : levels.get(levels.size() - 2).getCells()) { // the root has
                                                                                  // all cells in
                                                                                  // level rootlevel
                                                                                  // - 1 as its
                                                                                  // children and
                                                                                  // those children
                                                                                  // all have the
                                                                                  // root as parent
      oldTopLevelCell.setParent(topLevelCell);
      topLevelCell.addChild(oldTopLevelCell);
    }
    return topLevelCell;
  }

  private void createInitialRoot(T nextItem) throws Exception {
    HCTLevel<T> level = new HCTLevel<>(this);
    levels.add(level);
    HCTCell<T> cell = level.addCell();
    cell.addValue(nextItem);
  }

  @Override
  public IHCTCell<T> preemptiveCellSearch(List<IHCTCell<T>> ArrayCS, T nextItem, int curLevelNo,
      int levelNo) throws Exception {
    double dmin = dmin(nextItem, ArrayCS); // dmin of parent level
    if (curLevelNo == levelNo + 1) {
      return getMSCell(ArrayCS, nextItem);
    }
    List<IHCTCell<T>> newArrayCS = getAllCandidates(nextItem, dmin, ArrayCS);
    return preemptiveCellSearch(newArrayCS, nextItem, curLevelNo - 1, levelNo);
  }

  private IHCTCell<T> getMSCell(List<IHCTCell<T>> ArrayCS, T nextItem) throws Exception {
    IHCTCell<T> mSCell = null;
    double dist = Double.MAX_VALUE;

    for (IHCTCell<T> parent : ArrayCS) {
      for (IHCTCell<T> cell : parent.getChildren()) {
        if (cell.isCellDead()) {
          continue;
        }
        if (cell.getDistanceToNucleus(nextItem) < dist) {
          dist = cell.getDistanceToNucleus(nextItem);
          mSCell = cell;
        }
      }
    }
    return mSCell;
  }

  @Override
  public void remove(IHCTCell<T> cellO, T value, int levelNo) throws Exception {
    int topLevelNo = levels.size() - 1;
    List<HCTCell<T>> cells = levels.get(levels.size() - 1).getCells();

    IHCTCell<T> parentCell = cellO.getParent();
    if (cells.isEmpty() || levelNo > topLevelNo)
     {
      return; // experimental
    }
    if (!parentCell.containsValue(value)) {
      throw new Exception("Parent cell does not contain expected nucleus! Child cell: " + cellO);
    }

    T oldNucleusValue = parentCell.getNucleus().getValue();
    parentCell.removeValue(value);
    if (parentCell.isCellDead()) {
      if (levelNo == topLevelNo) {
        levels.remove(levels.get(topLevelNo));
      } else {
        parentCell.getParent().removeChild(parentCell);
        levels.get(levelNo).getCells().remove(parentCell);
        remove(parentCell, oldNucleusValue, levelNo + 1);
      }
    } else if (parentCell.isReadyForMitosis()) {
      doMitosis(levelNo, parentCell, oldNucleusValue);
    } else if (oldNucleusValue != parentCell.getNucleus().getValue()) {
      nucleusChanged(levelNo, parentCell, oldNucleusValue);
    }
  }

  @Override
  public String toString() {
    return String.format("HCT | #levels: %s", levels.size());
  }
/*
  private String print() throws Exception {

    StringBuilder sb = new StringBuilder();
    for (HCTLevel<T> level : levels) {
      sb.append("level : ").append(levels.indexOf(level)).append(" ");
      for (HCTCell<T> cell : level.getCells()) {
        sb.append(Joiner.on(", ").join(cell.getValues())).append(" | Parent (Nucleus): ");
        sb.append(cell.getNucleus().getValue());
        sb.append("......");
      }
      sb.append(System.lineSeparator());
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    } else {
      sb.append("{empty tree}");
    }
    return sb.toString();
  }*/

  private List<IHCTCell<T>> getAllCandidates(T other, double dmin, List<IHCTCell<T>> parents)
      throws Exception {
    List<IHCTCell<T>> candidates = new ArrayList<>();
    for (IHCTCell<T> parent : parents) {
      for (IHCTCell<T> cell : parent.getChildren()) {
        double distanceToNucleus = cell.getDistanceToNucleus(other);
        double coveringRadius = cell.getCoveringRadius();

        if (distanceToNucleus - coveringRadius <= dmin) {
          candidates.add(cell);
        }
      }
    }

    return candidates;
  }

  private void sanityCheck() throws Exception {
    for (HCTLevel<T> level : levels) {
      if (levels.indexOf(level) == levels.size() - 1) {
        return;
      }
      int sumOfChildren = 0;
      for (HCTCell<T> cell : levels.get(levels.indexOf(level) + 1).getCells()) {
        sumOfChildren += cell.getChildren().size();
      }
      if (level.getCells().size() != sumOfChildren) {
        throw new Exception("### ERROR child-relationship is broken. level: "
            + levels.indexOf(level) + ", #cells on this level: " + level.getCells().size()
            + ", #children on the upper level:" + sumOfChildren);
      }
    }
  }

  public HCTCell<T> getRoot() throws Exception {
    if (levels.get(levels.size() - 1).getCells().size() != 1) {
      throw new Exception("Root is ambiguous! # of cells on top level is "
          + levels.get(levels.size() - 1).getCells().size());
    }
    return levels.get(levels.size() - 1).getCells().get(0);
  }

  private long getNbrOfCellsInTree() {
    long nbrOfCells = 0;
    for (HCTLevel<T> level : levels) {
      nbrOfCells += level.getCells().size();
    }
    return nbrOfCells;
  }

  private double dmin(T other, List<IHCTCell<T>> arrayCS) throws Exception {
    double dmin = Double.MAX_VALUE;
    for (IHCTCell<T> cell : arrayCS) { // search only in selected cells
      if (cell.getDistanceToNucleus(other) < dmin) {
        dmin = cell.getDistanceToNucleus(other);
      }
    }
    return dmin;
  }

  CompactnessCalculation getCompactnessCalculation() {
    return compactnessCalculation;
  }

  DistanceCalculation<T> getDistanceCalculation() {
    return distanceCalculation;
  }

  public void traverseTreeHorizontal(TreeTraverserHorizontal<T> traverserHorizontal)
      throws Exception {
    for (HCTLevel<T> level : levels) {
      traverserHorizontal.newLevel();
      int valuesInLevel = 0;
      int valuesNotNullInLevel = 0;
      for (HCTCell<T> cell : level.getCells()) {
        traverserHorizontal.newCell();
        if (cell.getParent() != null) {
          traverserHorizontal.processValues(cell.getValues(), cell.getNucleus().getValue(),
              cell.getParent().getNucleus().getValue());
        } else {
          traverserHorizontal.processValues(cell.getValues(), cell.getNucleus().getValue(), null);
        }

        traverserHorizontal.endCell();
        valuesInLevel += cell.getValues().size();
        if (logger.getLevel() == Level.INFO) {
          for (T v : cell.getValues()) {
            if (v != null) {
              valuesNotNullInLevel++;
            }
          }
        }
      }
      traverserHorizontal.endLevel(levels.indexOf(level));
      logger.info("# of values in this level is according to HCT: " + valuesInLevel
          + " # not null values in this level according to HCT: " + valuesNotNullInLevel);
    }
    traverserHorizontal.finished();
  }

  public int traverse(IHCTCell<T> parentCell, int counter) {

    for (IHCTCell<T> cell : parentCell.getChildren()) {
      if (cell.getChildren().isEmpty()) {
        // reached lowest level
        counter += cell.getValues().size();
      } else {
        counter = traverse(cell, counter);
      }
    }
    return counter;
  }

  public IHCTCell<T> getRootCell() {
    return levels.get(levels.size() - 1).getCells().get(0);
  }
}
