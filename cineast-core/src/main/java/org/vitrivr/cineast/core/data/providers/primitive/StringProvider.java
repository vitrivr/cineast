package org.vitrivr.cineast.core.data.providers.primitive;

public interface StringProvider {
	
	default String getString(){
	  throw new UnsupportedOperationException("No string value specified");
	}
	
}
