package org.vitrivr.cineast.core.data.providers.primitive;

public class ByteTypeProvider extends ByteProviderImpl implements PrimitiveTypeProvider {

  public ByteTypeProvider(byte value) {
    super(value);
  }

  @Override
  public ProviderDataType getType() {
    return ProviderDataType.BYTE;
  }
  
  @Override
  public String getString() {
    return Byte.toString(getByte());
  }

}
