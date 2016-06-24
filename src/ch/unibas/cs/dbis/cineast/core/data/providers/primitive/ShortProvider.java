package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface ShortProvider {

	public static final short DEFAULT_SHORT = 0;
	
	default short getShort(){
		return DEFAULT_SHORT;
	}
	
}
