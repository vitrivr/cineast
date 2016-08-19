package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface PrimitiveTypeProvider extends BooleanProvider, ByteProvider, DoubleProvider, FloatArrayProvider, FloatProvider, IntArrayProvider, IntProvider, LongProvider, ShortProvider, StringProvider{

	ProviderDataType getType();
	
}
