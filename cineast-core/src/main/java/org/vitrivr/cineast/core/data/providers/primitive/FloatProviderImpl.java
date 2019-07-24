package org.vitrivr.cineast.core.data.providers.primitive;

public class FloatProviderImpl implements FloatProvider, DoubleProvider {

	private final float value;
	
	public FloatProviderImpl(float value){
		this.value = value;
	}

	@Override
	public float getFloat() {
		return this.value;
	}

	@Override
  public double getDouble() {
    return this.value;
  }

  @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(value);
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
		FloatProviderImpl other = (FloatProviderImpl) obj;
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return "FloatProviderImpl [value=" + value + "]";
	}
	
}
