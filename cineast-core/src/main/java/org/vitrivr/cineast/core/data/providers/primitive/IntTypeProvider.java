package org.vitrivr.cineast.core.data.providers.primitive;

public class IntTypeProvider extends IntProviderImpl implements PrimitiveTypeProvider{

	public IntTypeProvider(int value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.INT;
	}
	
	@Override
  public String getString() {
    return Integer.toString(getInt());
  }
	
}