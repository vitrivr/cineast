package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;

/**
 * @author silvan on 13.04.18.
 */
public interface BitSetProvider {
  default BitSet getBitSet() {
    throw new UnsupportedOperationException("No BitSet specified");
  }
}
