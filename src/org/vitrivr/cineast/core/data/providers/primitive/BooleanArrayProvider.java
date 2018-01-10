package org.vitrivr.cineast.core.data.providers.primitive;

/**
 * @author silvan on 18.12.17.
 */
public interface BooleanArrayProvider {
  default boolean[] getBooleanArray() {
    throw new UnsupportedOperationException("No boolean array specified");
  }
}
