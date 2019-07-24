package org.vitrivr.cineast.core.data.providers.primitive;

public interface IntArrayProvider {

	public static final int[] DEFAULT_INT_ARRAY = new int[]{};
	
	default int[] getIntArray(){
	  throw new UnsupportedOperationException("No int array specified");
	}
	
}
