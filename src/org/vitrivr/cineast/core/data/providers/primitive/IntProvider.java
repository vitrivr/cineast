package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface IntProvider {

	public static final int DEFAULT_INT = 0;
	
	default int getInt(){
		return DEFAULT_INT;
	}
	
}
