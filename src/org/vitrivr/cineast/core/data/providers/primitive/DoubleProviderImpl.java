package org.vitrivr.cineast.core.data.providers.primitive;

public class DoubleProviderImpl implements DoubleProvider, FloatProvider {

	private final double value;
	
	public DoubleProviderImpl(double value){
		this.value = value;
	}

	@Override
	public double getDouble() {
		return this.value;
	}

	@Override
	public float getFloat() {
		return (float)this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		DoubleProviderImpl other = (DoubleProviderImpl) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return "DoubleProviderImpl [value=" + value + "]";
	}
	
}
