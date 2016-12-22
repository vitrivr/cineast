package org.vitrivr.cineast.core.data.providers.primitive;

public interface PrimitiveTypeProvider
    extends BooleanProvider, ByteProvider, DoubleProvider, FloatArrayProvider, FloatProvider,
    IntArrayProvider, IntProvider, LongProvider, ShortProvider, StringProvider {

  ProviderDataType getType();

  public static Object getObject(PrimitiveTypeProvider t) {
    switch (t.getType()) {
    case BOOLEAN: {
      return t.getBoolean();
    }
    case BYTE: {
      return t.getByte();
    }
    case DOUBLE: {
      return t.getDouble();
    }
    case FLOAT: {
      return t.getFloat();
    }
    case FLOAT_ARRAY: {
      return t.getFloatArray();
    }
    case INT: {
      return t.getInt();
    }
    case INT_ARRAY: {
      return t.getIntArray();
    }
    case LONG: {
      return t.getLong();
    }
    case SHORT: {
      return t.getShort();
    }
    case STRING: {
      return t.getString();
    }
    case UNKNOWN:
    default:
      return null;

    }
  }

}
