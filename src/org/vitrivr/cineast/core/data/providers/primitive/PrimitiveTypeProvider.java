package org.vitrivr.cineast.core.data.providers.primitive;

public interface PrimitiveTypeProvider extends BooleanProvider, ByteProvider, DoubleProvider, FloatArrayProvider, FloatProvider, IntArrayProvider, IntProvider, LongProvider, ShortProvider, StringProvider{

	ProviderDataType getType();
	
}
