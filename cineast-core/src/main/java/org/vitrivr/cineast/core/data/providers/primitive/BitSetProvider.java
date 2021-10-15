package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;


public interface BitSetProvider {
  default BitSet getBitSet() {
    throw new UnsupportedOperationException("No BitSet specified");
  }
}
