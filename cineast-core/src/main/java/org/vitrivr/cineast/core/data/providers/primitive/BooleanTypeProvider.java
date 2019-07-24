package org.vitrivr.cineast.core.data.providers.primitive;

public class BooleanTypeProvider extends BooleanProviderImpl implements PrimitiveTypeProvider{

	public BooleanTypeProvider(boolean value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.BOOLEAN;
	}
	
	@Override
  public String getString() {
    return getBoolean() ? "true" : "false";
  }
	
}