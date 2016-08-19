package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface FloatProvider {

	public static final float DEFAULT_FLOAT = 0f;
	
	default float getFloat(){
		return DEFAULT_FLOAT;
	}
	
}
