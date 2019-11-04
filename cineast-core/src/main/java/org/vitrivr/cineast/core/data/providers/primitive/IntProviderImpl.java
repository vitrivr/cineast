package org.vitrivr.cineast.core.data.providers.primitive;

public class IntProviderImpl implements IntProvider, LongProvider, FloatProvider, DoubleProvider {

	private final int value;
	
	public IntProviderImpl(int value){
		this.value = value;
	}

	@Override
	public int getInt() {
		return this.value;
	}
	
	@Override
  public long getLong() {
    return this.value;
  }

  @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public double getDouble() {
		return value;
	}

	@Override
	public float getFloat() {
		return value;
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
		IntProviderImpl other = (IntProviderImpl) obj;
		if (value != other.value) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return String.format("IntProviderImpl [value=%s]", value);
	}
	
}
