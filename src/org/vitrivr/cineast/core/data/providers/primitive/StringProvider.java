package org.vitrivr.cineast.core.data.providers.primitive;

public interface StringProvider {

	public static final String DEFAULT_STRING = "";
	
	default String getString(){
		return DEFAULT_STRING;
	}
	
}
