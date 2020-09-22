package org.vitrivr.cineast.core.data;

import java.util.Set;

public interface Grid<T> {

  Grid<T> setElement(int x, int y, T element);

  T get(int x, int y);

  boolean isset(int x, int y);

  /**
   * returns and removes the element at (x,y).
   * @return the element at position (x,y) or null if there was no such element 
   */
  T remove(int x, int y);

  /**
   * compacts the grid by removing unnecessary data structures.
   */
  void compact();
  
  Set<Position> getKeySet();

}