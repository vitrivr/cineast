package org.vitrivr.cineast.core.data.providers.primitive;

public class NothingProvider implements PrimitiveTypeProvider{

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.UNKNOWN;
	}
	
}