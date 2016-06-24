package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

public interface ByteProvider {

	public static final byte DEFAULT_BYTE = 0;
	
	default byte getByte(){
		return DEFAULT_BYTE;
	}
	
}
