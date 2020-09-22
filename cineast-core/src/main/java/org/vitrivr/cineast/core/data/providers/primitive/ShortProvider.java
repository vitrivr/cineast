package org.vitrivr.cineast.core.data.providers.primitive;

public interface ShortProvider {
	
	default short getShort(){
	  throw new UnsupportedOperationException("No short value specified");
	}
	
}
