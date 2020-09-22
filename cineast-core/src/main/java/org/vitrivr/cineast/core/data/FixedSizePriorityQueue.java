package org.vitrivr.cineast.core.data;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * 
 * based on
 * http://stackoverflow.com/questions/7878026/is-there-a-priorityqueue-implementation-with-fixed-
 * capacity-and-custom-comparato
 */
public class FixedSizePriorityQueue<E> extends TreeSet<E> {

  private static final long serialVersionUID = 6510572768545573017L;
  private final int maxSize;

  private FixedSizePriorityQueue(int maxSize, Comparator<E> comparator) {
    super(comparator);
    if (maxSize < 0) {
      throw new IllegalArgumentException("maxSize must be positive");
    }
    this.maxSize = maxSize;
  }

  public static <T extends Comparable<? super T>> FixedSizePriorityQueue<T> create(int maxSize) {
    return new FixedSizePriorityQueue<T>(maxSize, Comparator.naturalOrder());
  }

  public static <T> FixedSizePriorityQueue<T> create(int maxSize, Comparator<T> comparator) {
    return new FixedSizePriorityQueue<T>(maxSize, comparator);
  }

  private final int elementsLeft() {
    return this.maxSize - this.size();
  }

  /**
   * @return true if element was added, false otherwise
   */
  @Override
  public boolean add(E e) {
    if (elementsLeft() <= 0 && size() == 0) {
      // max size was initiated to zero => just return false
      return false;
    } else if (elementsLeft() > 0) {
      // queue isn't full => add element and decrement elementsLeft
      boolean added = super.add(e);
      return added;
    } else {
      // there is already 1 or more elements => compare to the least
      int compared = super.comparator().compare(this.last(), e);
      if (compared > 0) {
        // new element is larger than the least in queue => pull the least and add new one to queue
        pollLast();
        super.add(e);
        return true;
      } else {
        // new element is less than the least in queue => return false
        return false;
      }
    }
  }

  public int getMaxSize() {
    return this.maxSize;
  }

}
