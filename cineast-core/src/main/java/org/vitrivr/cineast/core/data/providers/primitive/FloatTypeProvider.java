package org.vitrivr.cineast.core.data.providers.primitive;

public class FloatTypeProvider extends FloatProviderImpl implements PrimitiveTypeProvider{

	public FloatTypeProvider(float value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.FLOAT;
	}
	
	@Override
  public String getString() {
    return Float.toString(getFloat());
  }
	
}