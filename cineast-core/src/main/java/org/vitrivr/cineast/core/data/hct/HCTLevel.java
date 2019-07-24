package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HCTLevel<T extends Comparable<T>> implements Serializable {

  private static final long serialVersionUID = 2430734223457861476L;

    private final HCT<T> hct;

    private List<HCTCell<T>> cells = new ArrayList<>();

    public HCTLevel(HCT<T> hct) {
        this.hct = hct;
    }

    public List<HCTCell<T>> getCells() {
        return cells;
    }

    public void removeCell(IHCTCell<T> cell) {
        cells.remove(cell);
    }

    public HCTCell<T> addCell() {
        HCTCell<T> cell = new HCTCell<T>(hct);
        cells.add(cell);
        return cell;
    }

    public void addCell(HCTCell<T> cell) {
        cells.add(cell);
    }

    @Override
    public String toString() {
        return String.format("HCTLevel | #cells: %s", cells.size());
    }
}
