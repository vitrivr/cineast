package org.vitrivr.cineast.core.util.distance;

/**
 * @author silvan on 18.12.17.
 */
public class BooleanHammingDistance implements Distance<boolean[]> {
  
  @Override
  public double applyAsDouble(boolean[] one, boolean[] two) {
    double sum = 0;
    for (int i = 0; i < one.length; i++) {
      if (one[i] != two[i]) {
        sum++;
      }
    }
    return sum;
  }
}
