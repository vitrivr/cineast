package org.vitrivr.cineast.core.data.providers.primitive;

public interface FloatProvider {
	
	default float getFloat(){
	  throw new UnsupportedOperationException("No float value specified");
	}
	
}
