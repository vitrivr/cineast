package org.vitrivr.cineast.core.data.providers.primitive;

public class DoubleTypeProvider extends DoubleProviderImpl implements PrimitiveTypeProvider{

	public DoubleTypeProvider(double value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.DOUBLE;
	}
	
	@Override
  public String getString() {
    return Double.toString(getDouble());
  }
	
}