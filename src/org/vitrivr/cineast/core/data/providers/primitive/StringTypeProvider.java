package org.vitrivr.cineast.core.data.providers.primitive;

public class StringTypeProvider extends StringProviderImpl implements PrimitiveTypeProvider{

	public StringTypeProvider(String value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.STRING;
	}
	
}