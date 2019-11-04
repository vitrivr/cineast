package org.vitrivr.cineast.core.data.providers.primitive;

public class NothingProvider implements PrimitiveTypeProvider{

  public static final NothingProvider INSTANCE = new NothingProvider();
  
	@Override
	public ProviderDataType getType() {
		return ProviderDataType.UNKNOWN;
	}

  @Override
  public String toString() {
    return "NothingProvider";
  }
	
}