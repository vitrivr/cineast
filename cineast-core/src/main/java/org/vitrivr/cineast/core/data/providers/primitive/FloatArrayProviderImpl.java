package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;

public class FloatArrayProviderImpl implements FloatArrayProvider {

	private final float[] value;
	
	public FloatArrayProviderImpl(float[] value){
		if(value == null){
			throw new NullPointerException("float[] cannot be null");
		}
		this.value = value;
	}

	@Override
	public float[] getFloatArray() {
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
		if (this == obj) {
      return true;
    }
		if (obj == null) {
      return false;
    }
		if (getClass() != obj.getClass()) {
      return false;
    }
		FloatArrayProviderImpl other = (FloatArrayProviderImpl) obj;
		if (!Arrays.equals(value, other.value)) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return "FloatArrayProviderImpl [value=" + Arrays.toString(value) + "]";
	}
	
}
