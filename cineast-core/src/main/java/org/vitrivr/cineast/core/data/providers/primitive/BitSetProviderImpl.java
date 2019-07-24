package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;

import java.util.Arrays;

public class BitSetProviderImpl implements BitSetProvider {

  private final BitSet value;

  public BitSetProviderImpl(BitSet value) {
    this.value = value;
  }

  public BitSet getValue() {
    return value;
  }

  public static BitSetProvider fromString(String string){
    String raw = string.substring(1, string.length() - 1);
    BitSet bitSet = new BitSet(64); //TODO We assume fixed size here
    Arrays.stream(raw.split(",")).forEach(el -> bitSet.set(Integer.parseInt(el)));
    return new BitSetProviderImpl(bitSet);
  }

  @Override
  public BitSet getBitSet() {
    return value;
  }

  @Override
  public String toString() {
    return "BitSetProviderImpl{" +
        "value=" + value +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BitSetProviderImpl)) {
      return false;
    }

    BitSetProviderImpl that = (BitSetProviderImpl) o;

    return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
  }

  @Override
  public int hashCode() {
    return getValue() != null ? getValue().hashCode() : 0;
  }

}
