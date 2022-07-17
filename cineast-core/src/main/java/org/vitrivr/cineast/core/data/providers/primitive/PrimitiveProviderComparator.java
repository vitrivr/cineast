package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Comparator;

public class PrimitiveProviderComparator implements Comparator<PrimitiveTypeProvider> {

  @Override
  public int compare(PrimitiveTypeProvider o1, PrimitiveTypeProvider o2) {

    if (o1 == o2) {
      return 0;
    }

    if (o1.getType() == ProviderDataType.FLOAT_ARRAY || o1.getType() == ProviderDataType.INT_ARRAY) {
      if (o2.getType() == ProviderDataType.FLOAT_ARRAY || o2.getType() == ProviderDataType.INT_ARRAY) {
        return 0;
      } else {
        return -1;
      }
    } else if (o2.getType() == ProviderDataType.FLOAT_ARRAY || o2.getType() == ProviderDataType.INT_ARRAY) {
      return 1;
    }

    if (o1.getType() == ProviderDataType.STRING) {
      if (o2.getType() == ProviderDataType.STRING) {
        return Comparator.<String>naturalOrder().compare(o1.getString(), o2.getString());
      }
      return -1;
    } else if (o2.getType() == ProviderDataType.STRING) {
      return 1;
    }

    if (o1.getType() != o2.getType()) {
      return 0;
    }

    switch (o1.getType()) {
      case BOOLEAN:
        return Boolean.compare(o1.getBoolean(), o2.getBoolean());
      case BYTE:
        return Byte.compare(o1.getByte(), o2.getByte());
      case SHORT:
        return Short.compare(o1.getShort(), o2.getShort());
      case INT:
        return Integer.compare(o1.getInt(), o2.getInt());
      case FLOAT:
        return Float.compare(o1.getFloat(), o2.getFloat());
      case LONG:
        return Long.compare(o1.getLong(), o2.getLong());
      case DOUBLE:
        return Double.compare(o1.getDouble(), o2.getDouble());
      case BITSET:
        return Integer.compare(o1.getBitSet().cardinality(), o2.getBitSet().cardinality());
    }

    return 0;

  }
}
