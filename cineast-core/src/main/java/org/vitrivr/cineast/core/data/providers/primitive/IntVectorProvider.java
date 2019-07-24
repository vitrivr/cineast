package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class IntVectorProvider extends IntArrayProviderImpl implements PrimitiveTypeProvider{

	public IntVectorProvider(int[] array) {
		super(array);
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