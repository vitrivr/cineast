package org.vitrivr.cineast.core.data.providers.primitive;

public interface DoubleProvider {
	
	default double getDouble(){
	  throw new UnsupportedOperationException("No double value specified");
	}
	
}
