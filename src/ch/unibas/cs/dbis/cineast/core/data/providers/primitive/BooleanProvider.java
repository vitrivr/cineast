package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface BooleanProvider {

	public static final boolean DEFAULT_BOOLEAN = false;
	
	default boolean getBoolean(){
		return DEFAULT_BOOLEAN;
	}
	
}
