package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface IntArrayProvider {

	public static final int[] DEFAULT_INT_ARRAY = new int[]{};
	
	default int[] getIntArray(){
		return DEFAULT_INT_ARRAY;
	}
	
}
