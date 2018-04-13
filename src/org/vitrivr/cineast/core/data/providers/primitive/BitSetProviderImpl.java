package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;

public class BitSetProviderImpl implements BitSetProvider {

	private final BitSet value;

	public BitSetProviderImpl(BitSet value){
		this.value = value;
	}

	public BitSet getValue() {
		return value;
	}

	@Override
	public BitSet getBitSet() {
		return value;
	}

	@Override
	public String toString() {
		return "BitSetProviderImpl{" +
				"value=" + value +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BitSetProviderImpl)) {
			return false;
		}

		BitSetProviderImpl that = (BitSetProviderImpl) o;

		return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;
	}

	@Override
	public int hashCode() {
		return getValue() != null ? getValue().hashCode() : 0;
	}

}
