package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Comparator;

public class PrimitiveProviderComparator implements Comparator<PrimitiveTypeProvider> {

  @Override
  public int compare(PrimitiveTypeProvider o1, PrimitiveTypeProvider o2) {

    if(o1 == o2){
      return 0;
    }

    if (o1.getType() == ProviderDataType.FLOAT_ARRAY || o1.getType() == ProviderDataType.INT_ARRAY){
      if(o2.getType() == ProviderDataType.FLOAT_ARRAY || o2.getType() == ProviderDataType.INT_ARRAY){
        return 0;
      } else {
        return -1;
      }
    } else if (o2.getType() == ProviderDataType.FLOAT_ARRAY || o2.getType() == ProviderDataType.INT_ARRAY){
      return 1;
    }

    if (o1.getType() == ProviderDataType.STRING){
      if(o2.getType() == ProviderDataType.STRING){
        return Comparator.<String>naturalOrder().compare(o1.getString(), o2.getString());
      }
      return -1;
    }else if(o2.getType() == ProviderDataType.STRING){
      return 1;
    }

    return Comparator.<Comparable>naturalOrder().compare((Comparable) PrimitiveTypeProvider.getObject(o1), (Comparable) PrimitiveTypeProvider.getObject(o2));
  }
}
