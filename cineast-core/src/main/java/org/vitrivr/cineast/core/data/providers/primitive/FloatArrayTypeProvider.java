package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.Arrays;
import java.util.List;

public class FloatArrayTypeProvider extends FloatArrayProviderImpl
    implements PrimitiveTypeProvider {

  public FloatArrayTypeProvider(float[] value) {
    super(value);
  }

  @Override
  public ProviderDataType getType() {
    return ProviderDataType.FLOAT_ARRAY;
  }
  
  @Override
  public String getString() {
    return Arrays.toString(getFloatArray());
  }

  public static FloatArrayTypeProvider fromList(List<Float> list){
    float[] array = new float[list.size()];

    int i = 0;

    for(Float f : list){
      array[i++] = f;
    }

    return new FloatArrayTypeProvider(array);

  }


  public static PrimitiveTypeProvider fromDoubleList(List<Double> list) {

    float[] array = new float[list.size()];

    int i = 0;

    for(Double d : list){
      array[i++] = d.floatValue();
    }

    return new FloatArrayTypeProvider(array);

  }
}
