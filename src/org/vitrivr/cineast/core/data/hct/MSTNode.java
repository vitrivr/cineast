package org.vitrivr.cineast.core.data.hct;

import java.io.Serializable;

/**
 * Created by silvanstich on 13.09.16.
 */
public class MSTNode<T extends Comparable<T>> implements IMSTNode<T>, Serializable {

  private static final long serialVersionUID = 4318595957907792761L;
  private final T value;
  private final HCT<T> hct;

  public MSTNode(T value, HCT<T> hct) {
    this.value = value;
    this.hct = hct;
  }

  @Override
  public double distance(IMSTNode<T> other) {
    return hct.getDistanceCalculation().distance(value, other.getValue());
  };

  @Override
  public double distance(T otherValue) {
    return hct.getDistanceCalculation().distance(value, otherValue);
  };

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("MSTNode | value: %s >", value.toString());
  }

}
