package org.vitrivr.cineast.core.data.providers.primitive;

public class ShortProviderImpl implements ShortProvider, IntProvider, LongProvider {

	private final short value;
	
	public ShortProviderImpl(short value){
		this.value = value;
	}

	@Override
	public short getShort() {
		return this.value;
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
		ShortProviderImpl other = (ShortProviderImpl) obj;
		if (value != other.value) {
      return false;
    }
		return true;
	}

	@Override
	public String toString() {
		return String.format("ShortProviderImpl [value=%s]", value);
	}
}
