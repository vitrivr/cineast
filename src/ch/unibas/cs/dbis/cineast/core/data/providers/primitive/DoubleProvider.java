package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface DoubleProvider {

	public static final double DEFUALT_DOUBLE = 0d;
	
	default double getDouble(){
		return DEFUALT_DOUBLE;
	}
	
}
