package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

/**
 * @author silvan on 18.12.17.
 */
public class BooleanArrayProviderImpl implements BooleanArrayProvider {
  
  private final boolean[] value;
  
  public BooleanArrayProviderImpl(boolean[] value) {
    if (value == null) {
      throw new NullPointerException();
    }
    this.value = value;
  }
  
  @Override
  public String toString() {
    return "BooleanArrayProviderImpl{" +
        "value=" + Arrays.toString(value) +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BooleanArrayProviderImpl)) return false;
    
    BooleanArrayProviderImpl that = (BooleanArrayProviderImpl) o;
    
    return Arrays.equals(value, that.value);
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }
  
  @Override
  public boolean[] getBooleanArray() {
    return this.value;
    
  }
}
