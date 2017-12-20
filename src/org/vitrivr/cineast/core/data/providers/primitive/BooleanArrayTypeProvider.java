package org.vitrivr.cineast.core.data.providers.primitive;

/**
 * @author silvan on 18.12.17.
 */
public class BooleanArrayTypeProvider extends BooleanArrayProviderImpl implements PrimitiveTypeProvider {
  
  public BooleanArrayTypeProvider(boolean[] value) {
    super(value);
  }
  
  @Override
  public ProviderDataType getType() {
    return ProviderDataType.BOOLEAN_ARRAY;
  }
}
