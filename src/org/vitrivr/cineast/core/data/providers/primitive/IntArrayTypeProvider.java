package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;
import java.util.List;

public class IntArrayTypeProvider extends IntArrayProviderImpl implements PrimitiveTypeProvider {

  public IntArrayTypeProvider(int[] value) {
    super(value);
  }



  @Override
  public ProviderDataType getType() {
    return ProviderDataType.INT_ARRAY;
  }

  @Override
  public String getString() {
    return Arrays.toString(getIntArray());
  }


  public static PrimitiveTypeProvider fromList(List<Integer> list) {

    int[] array = new int[list.size()];

    int i = 0;

    for(Integer in : list){
      array[i++] = in;
    }

    return new IntArrayTypeProvider(array);
  }

  public static PrimitiveTypeProvider fromLongList(List<Long> list) {

    int[] array = new int[list.size()];

    int i = 0;

    for(Long l : list){
      array[i++] = l.intValue();
    }

    return new IntArrayTypeProvider(array);

  }

}
