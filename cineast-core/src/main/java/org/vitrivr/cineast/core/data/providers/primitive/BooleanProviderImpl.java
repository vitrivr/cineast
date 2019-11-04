package org.vitrivr.cineast.core.data.providers.primitive;

public class BooleanProviderImpl implements BooleanProvider {

	private final boolean value;
	
	public BooleanProviderImpl(boolean value){
		this.value = value;
	}
	
	@Override
	public boolean getBoolean() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value ? 1231 : 1237);
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
		BooleanProviderImpl other = (BooleanProviderImpl) obj;
		if (value != other.value) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return "BooleanProviderImpl [value=" + value + "]";
	}
}
