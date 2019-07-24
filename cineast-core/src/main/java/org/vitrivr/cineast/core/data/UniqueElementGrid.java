package org.vitrivr.cineast.core.data;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashBiMap;

public class UniqueElementGrid<T> implements Grid<T> {

  private final HashBiMap<Position, T> map = HashBiMap.create();

  @Override
  public Grid<T> setElement(int x, int y, T element) {
    map.forcePut(new Position(x, y), element);
    return this;
  }
  
  public Grid<T> setElement(Position p, T element) {
    map.forcePut(p, element);
    return this;
  }

  @Override
  public T get(int x, int y) {
    return map.get(new Position(x, y));
  }
  
  public T get(Position p){
    if(p == null){
      return null;
    }
    return map.get(p);
  }

  /**
   * returns the grid position of the specified element or null if the element is not part of the
   * map
   * 
   * @param element
   * @return
   */
  public Position getPosition(T element) {
    return map.inverse().get(element);
  }

  @Override
  public boolean isset(int x, int y) {
    return map.containsKey(new Position(x, y));
  }

  public boolean isset(Position p) {
    if (p == null) {
      return false;
    }
    return map.containsKey(p);
  }

  @Override
  public T remove(int x, int y) {
    return map.remove(new Position(x, y));
  }

  public T remove(Position p) {
    return map.remove(p);
  }

  public Position remove(T element) {
    return map.inverse().remove(element);
  }

  @Override
  public void compact() {
    // not supported
  }

  @Override
  public Set<Position> getKeySet() {
    HashSet<Position> _return = new HashSet<>();
    _return.addAll(_return);
    return _return;
  }

}
