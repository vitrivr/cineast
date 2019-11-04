package org.vitrivr.cineast.core.data.providers.primitive;

public interface IntProvider {
	
	default int getInt(){
	  throw new UnsupportedOperationException("No int value specified");
	}
	
}
