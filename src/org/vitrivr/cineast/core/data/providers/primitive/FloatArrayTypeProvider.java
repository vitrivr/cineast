package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class FloatArrayTypeProvider extends FloatArrayProviderImpl
    implements PrimitiveTypeProvider {

  public FloatArrayTypeProvider(float[] value) {
    super(value);
  }

  @Override
  public ProviderDataType getType() {
    return ProviderDataType.FLOAT_ARRAY;
  }
  
  @Override
  public String getString() {
    return Arrays.toString(getFloatArray());
  }

}
