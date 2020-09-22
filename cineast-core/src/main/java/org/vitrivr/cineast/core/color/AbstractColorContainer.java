package org.vitrivr.cineast.core.color;

import org.vitrivr.cineast.core.data.ReadableFloatVector;

public abstract class AbstractColorContainer<T extends AbstractColorContainer<?>> implements
    ReadableFloatVector, Comparable<T> {

  @Override
  public int getElementCount() {
    return 3;
  }

  @Override
  public int compareTo(T o) {
    int compare = Float.compare(o.getElement(0), getElement(0));
    if (compare != 0) {
      return compare;
    }
    compare = Float.compare(o.getElement(1), getElement(1));
    if (compare != 0) {
      return compare;
    }
    return Float.compare(o.getElement(2), getElement(2));
  }

}
