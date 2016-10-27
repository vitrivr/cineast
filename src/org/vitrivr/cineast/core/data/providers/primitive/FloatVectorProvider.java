package org.vitrivr.cineast.core.data.providers.primitive;

public class FloatVectorProvider extends FloatArrayProviderImpl implements PrimitiveTypeProvider {

	public FloatVectorProvider(float[] array) {
		super(array);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.FLOAT_ARRAY;
	}

}