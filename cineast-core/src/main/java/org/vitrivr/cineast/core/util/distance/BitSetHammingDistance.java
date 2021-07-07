package org.vitrivr.cineast.core.util.distance;

import com.googlecode.javaewah.datastructure.BitSet;


public class BitSetHammingDistance implements Distance<BitSet> {
  
  @Override
  public double applyAsDouble(BitSet one, BitSet two) {
    return one.xorcardinality(two);
  }
}
