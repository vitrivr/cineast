package org.vitrivr.cineast.core.data.providers.primitive;

public class ShortTypeProvider extends ShortProviderImpl implements PrimitiveTypeProvider {

  public ShortTypeProvider(short value) {
    super(value);
  }

  @Override
  public ProviderDataType getType() {
   return ProviderDataType.SHORT;
  }
  
  @Override
  public String getString() {
    return Short.toString(getShort());
  }

}
