package org.vitrivr.cineast.core.data.providers.primitive;

public interface BooleanProvider {
	
	default boolean getBoolean(){
		throw new UnsupportedOperationException("No boolean value specified");
	}
	
}
