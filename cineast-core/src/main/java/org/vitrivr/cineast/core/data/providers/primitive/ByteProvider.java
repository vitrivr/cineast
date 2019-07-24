package org.vitrivr.cineast.core.data.providers.primitive;

public interface ByteProvider {

	default byte getByte(){
	  throw new UnsupportedOperationException("No byte value specified");
	}
	
}
