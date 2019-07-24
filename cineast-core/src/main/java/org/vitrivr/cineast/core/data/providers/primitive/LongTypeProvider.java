package org.vitrivr.cineast.core.data.providers.primitive;

public class LongTypeProvider extends LongProviderImpl implements PrimitiveTypeProvider{

	public LongTypeProvider(long value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.LONG;
	}
	
	@Override
  public String getString() {
    return Long.toString(getLong());
  }
	
}