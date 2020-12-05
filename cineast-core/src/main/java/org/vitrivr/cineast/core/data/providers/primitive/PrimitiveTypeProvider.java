package org.vitrivr.cineast.core.data.providers.primitive;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.javaewah.datastructure.BitSet;

import java.util.List;

public interface PrimitiveTypeProvider
    extends BooleanProvider,
    ByteProvider,
    DoubleProvider,
    FloatArrayProvider,
    FloatProvider,
    IntArrayProvider,
    IntProvider,
    LongProvider,
    ShortProvider,
    StringProvider,
    BitSetProvider {

  /**
   * Convenience-method to convert to object
   */
  default Object toObject() {
    return PrimitiveTypeProvider.getObject(this);
  }

  /**
   * Casts an int[] to float[] if need be
   */
  public static float[] getSafeFloatArray(PrimitiveTypeProvider provider) {
    if (provider.getType().equals(ProviderDataType.FLOAT_ARRAY)) {
      return provider.getFloatArray();
    }
    if (provider.getType().equals(ProviderDataType.INT_ARRAY)) {
      float[] query = new float[provider.getIntArray().length];
      for (int i = 0; i < query.length; i++) {
        query[i] = provider.getIntArray()[i];
      }
      return query;
    }
    if (provider.getType().equals(ProviderDataType.FLOAT)) {
      return new float[]{provider.getFloat()};
    }
    if (provider.getType().equals(ProviderDataType.INT)) {
      return new float[]{provider.getInt()};
    }
    if (provider.getType().equals(ProviderDataType.DOUBLE)) {
      return new float[]{(float) provider.getDouble()};
    }
    if (provider.getType().equals(ProviderDataType.LONG)) {
      return new float[]{(float) provider.getLong()};
    }
    throw new RuntimeException(provider.getType().toString());
  }

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
      case BITSET: {
        return t.getBitSet();
      }
      case UNKNOWN:
      default:
        return null;
    }
  }

  public static PrimitiveTypeProvider fromObject(Object o) {
    if (o == null) {
      return NothingProvider.INSTANCE;
    }

    Class<? extends Object> c = o.getClass();

    if (c == Boolean.class) {
      return new BooleanTypeProvider((Boolean) o);
    }

    if (c == Byte.class) {
      return new ByteTypeProvider(((Number) o).byteValue());
    }

    if (c == Double.class) {
      return new DoubleTypeProvider(((Number) o).doubleValue());
    }

    if (c == Float.class) {
      return new FloatTypeProvider(((Number) o).floatValue());
    }

    if (c == Integer.class) {
      return new IntTypeProvider(((Number) o).intValue());
    }

    if (c == Long.class) {
      return new LongTypeProvider(((Number) o).longValue());
    }

    if (c == Short.class) {
      return new ShortTypeProvider(((Number) o).shortValue());
    }

    if (c == String.class) {
      return new StringTypeProvider((String) o);
    }

    if (c == int[].class) {
      return new IntArrayTypeProvider((int[]) o);
    }

    if (c == float[].class) {
      return new FloatArrayTypeProvider((float[]) o);
    }

    if (c == BitSet.class) {
      return new BitSetTypeProvider((BitSet) o);
    }

    if (List.class.isAssignableFrom(c)) {
      List<?> list = (List<?>) o;
      if (list.isEmpty()) {
        return new FloatArrayTypeProvider(new float[]{});
      }
      Object first = list.get(0);
      outerif:
      if (first.getClass() == Integer.class) {
        int[] arr = new int[list.size()];
        int i = 0;
        for (Object x : list) {
          if (x.getClass() != Integer.class) {
            break outerif;
          }
          arr[i++] = (Integer) x;
        }
        return new IntArrayTypeProvider(arr);
      }

      if (first.getClass() == Float.class) {
        float[] arr = new float[list.size()];
        int i = 0;
        for (Object x : list) {
          arr[i++] = ((Number) x).floatValue();
        }
        return new FloatArrayTypeProvider(arr);
      }

      if (first.getClass() == Double.class) {
        float[] arr = new float[list.size()];
        int i = 0;
        for (Object x : list) {
          arr[i++] = (float) ((Number) x).doubleValue();
        }
        return new FloatArrayTypeProvider(arr);
      }
    }

    return NothingProvider.INSTANCE;
  }

  public static PrimitiveTypeProvider fromJSON(JsonNode json) {
    if (json == null) {
      return NothingProvider.INSTANCE;
    }

    if (json.isTextual()) {
      return new StringTypeProvider(json.asText());
    }

    if (json.isInt()) {
      return new IntTypeProvider(json.asInt());
    }

    if (json.isLong()) {
      return new LongTypeProvider(json.asLong());
    }

    if (json.isFloat()) {
      return new FloatTypeProvider(json.floatValue());
    }

    if (json.isDouble()) {
      return new DoubleTypeProvider(json.doubleValue());
    }

    if (json.isBoolean()) {
      return new BooleanTypeProvider(json.asBoolean());
    }

    // TODO are arrays relevant here?
    return NothingProvider.INSTANCE;
  }

  ProviderDataType getType();
}
