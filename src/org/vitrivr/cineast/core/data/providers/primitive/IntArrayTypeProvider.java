package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class IntArrayTypeProvider extends IntArrayProviderImpl implements PrimitiveTypeProvider {

  public IntArrayTypeProvider(int[] value) {
    super(value);
  }

  @Override
  public ProviderDataType getType() {
    return ProviderDataType.INT_ARRAY;
  }

  @Override
  public String getString() {
    return Arrays.toString(getIntArray());
  }
  
}
