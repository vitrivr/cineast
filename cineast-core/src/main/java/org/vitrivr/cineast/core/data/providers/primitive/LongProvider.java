package org.vitrivr.cineast.core.data.providers.primitive;

public interface LongProvider {
	
	default long getLong(){
	  throw new UnsupportedOperationException("No long value specified");
	}
	
}
