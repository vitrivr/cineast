package org.vitrivr.cineast.core.data.providers.primitive;

public class StringProviderImpl implements StringProvider {

	private final String value;
	
	public StringProviderImpl(String value){
		if(value == null){
			throw new NullPointerException("String cannot be null");
		}
		this.value = value;
	}

	@Override
	public String getString() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		StringProviderImpl other = (StringProviderImpl) obj;
		if (value == null) {
			if (other.value != null) {
        return false;
      }
		} else if (!value.equals(other.value)) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return String.format("StringProviderImpl [value=%s]", value);
	}
}
