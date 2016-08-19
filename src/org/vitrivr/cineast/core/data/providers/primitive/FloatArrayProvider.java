package org.vitrivr.cineast.core.data.providers.primitive;

public interface FloatArrayProvider {

	public static final float[] DEFAULT_FLOAT_ARRAY = new float[]{};
	
	default float[] getFloatArray(){
		return DEFAULT_FLOAT_ARRAY;
	}
	
}
