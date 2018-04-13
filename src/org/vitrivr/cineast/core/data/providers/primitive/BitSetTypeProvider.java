package org.vitrivr.cineast.core.data.providers.primitive;

import com.googlecode.javaewah.datastructure.BitSet;

public class BitSetTypeProvider extends BitSetProviderImpl implements PrimitiveTypeProvider{

	public BitSetTypeProvider(BitSet value) {
		super(value);
	}

	@Override
	public ProviderDataType getType() {
		return ProviderDataType.BITSET;
	}
}