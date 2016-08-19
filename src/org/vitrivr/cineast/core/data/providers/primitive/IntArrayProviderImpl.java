package ch.unibas.cs.dbis.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class IntArrayProviderImpl implements IntArrayProvider {

	private final int[] value;
	
	public IntArrayProviderImpl(int[] value){
		if(value == null){
			throw new NullPointerException("int[] cannot be null");
		}
		this.value = value;
	}

	@Override
	public int[] getIntArray() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntArrayProviderImpl other = (IntArrayProviderImpl) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("IntArrayProviderImpl [value=%s]", Arrays.toString(value));
	}
	
}
