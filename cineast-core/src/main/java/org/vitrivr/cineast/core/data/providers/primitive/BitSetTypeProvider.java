package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;

import java.util.Arrays;
import java.util.List;

public class BitSetTypeProvider implements PrimitiveTypeProvider {

  private final BitSet value;

  public BitSetTypeProvider(BitSet value) {
    this.value = value;
  }

  public BitSet getValue() {
    return value;
  }


  public static BitSetTypeProvider fromBooleanList(List<Boolean> list){
    final BitSet bitSet = new BitSet(list.size());
    for (int i = 0; i< list.size(); i++) {
      bitSet.set(i, list.get(i));
    }
    return new BitSetTypeProvider(bitSet);
  }

  public static BitSetTypeProvider fromBooleanArray(boolean[] array){
    final BitSet bitSet = new BitSet(array.length);
    for (int i = 0; i< array.length; i++) {
      bitSet.set(i, array[i]);
    }
    return new BitSetTypeProvider(bitSet);
  }

  public static BitSetTypeProvider fromString(String string){
    final String raw = string.substring(1, string.length() - 1);
    final BitSet bitSet = new BitSet(64); //TODO We assume fixed size here
    Arrays.stream(raw.split(",")).forEach(el -> bitSet.set(Integer.parseInt(el)));
    return new BitSetTypeProvider(bitSet);
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
    if (!(o instanceof BitSetTypeProvider)) {
      return false;
    }

    BitSetTypeProvider that = (BitSetTypeProvider) o;

    return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
  }

  @Override
  public int hashCode() {
    return getValue() != null ? getValue().hashCode() : 0;
  }

  @Override
  public ProviderDataType getType() {
    return ProviderDataType.BITSET;
  }
}
