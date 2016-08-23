package org.vitrivr.cineast.core.data.providers.primitive;

public interface LongProvider {

	public static final long DEFAULT_LONG = 0L;
	
	default long getLong(){
		return DEFAULT_LONG;
	}
	
}
