package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HCTCell<T extends Comparable<T>> implements IHCTCell<T>, Serializable {

  private static final long serialVersionUID = -2074417688119894358L;
  private final HCT<T> hct;

  private IMST<T> mst;
  private IHCTCell<T> parent;
  private List<IHCTCell<T>> children = new ArrayList<>();

  HCTCell(HCT<T> hct) {
    this.hct = hct;
    mst = new MST<>(hct);

  }

  private HCTCell(IMST<T> mst, IHCTCell<T> parent, HCT<T> hct) {
    this.hct = hct;
    this.mst = new MST<>(hct);
    this.mst = mst;
    this.parent = parent;
  }

  public void addValue(T value) throws Exception {
    mst.add(value);
  }

  public void removeValue(T value) throws Exception {
    mst.remove(value);
  }

  public double getDistanceToNucleus(T other) throws Exception {
    return mst.getNucleus().distance(other);
  }

  public double getCoveringRadius() throws Exception {
    return mst.getCoveringRadius();
  }

  public IHCTCell<T> getParent() {
    return parent;
  }

  public void setParent(IHCTCell<T> parent) {
    this.parent = parent;
  }

  public List<IHCTCell<T>> getChildren() {
    return children;
  }

  public boolean isReadyForMitosis() {
    return mst.isReadyForMitosis();
  }

  public List<HCTCell<T>> mitosis() throws Exception {
    List<IMST<T>> msts = mst.mitosis();
    List<HCTCell<T>> newCells = new ArrayList<>();
    for (IMST<T> mst : msts) {
      HCTCell<T> newCell = new HCTCell<T>(mst, parent, hct);
      newCells.add(newCell);
      if (parent != null)
        parent.addChild(newCell);

    }
    for (IHCTCell<T> child : children) {
      for (HCTCell<T> newCell : newCells) {
        if (newCell.getValues().contains(child.getNucleus().getValue())) {
          newCell.addChild(child);
          child.setParent(newCell);
          break;
        }
      }
    }
    return newCells;

  }

  public IMSTNode<T> getNucleus() throws Exception {
    return mst.getNucleus();
  }

  @Override
  public void addChild(IHCTCell<T> child) {
    if (!children.contains(child)) {
      children.add(child);
    }
  }

  @Override
  public boolean containsValue(T value) {
    return mst.containsValue(value);
  }

  @Override
  public boolean isCellDead() {
    return mst.isCellDead();
  }

  @Override
  public void removeChild(IHCTCell<T> child) {
    children.remove(child);
  }

  public String toString() {
    try {
      return String.format("HCTCell | isCellDead: %s | isReadyMitosis: %s | Nucleus: <%s>",
          isCellDead(), isReadyForMitosis(), getNucleus());
    } catch (Exception e) {
      return String.format("HCTCell | isCellDead: %s | isReadyMitosis: %s | Nucleus: <%s>",
          isCellDead(), isReadyForMitosis(),
          "###Error while getting the nucleus! " + e.getMessage());
    }

  }

  public List<T> getValues() {
    return mst.getValues();
  }

}
