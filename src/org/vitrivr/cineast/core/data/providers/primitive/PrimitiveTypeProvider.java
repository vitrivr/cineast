package org.vitrivr.cineast.core.data.providers.primitive;

import java.util.List;

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
  
  public static PrimitiveTypeProvider fromObject(Object o){
    if(o == null){
      return NothingProvider.INSTANCE;
    }
    
    Class<? extends Object> c = o.getClass();
    
    if(c == Boolean.class){
      return new BooleanTypeProvider((Boolean)o);
    }
    
    if(c == Byte.class){
      return new ByteTypeProvider((Byte)o);
    }
    
    if(c == Double.class){
      return new DoubleTypeProvider((Double)o);
    }
    
    if(c == Float.class){
      return new FloatTypeProvider((Float)o);
    }
    
    if(c == Integer.class){
      return new IntTypeProvider((Integer)o);
    }
    
    if(c == Long.class){
      return new LongTypeProvider((Long)o);
    }
    
    if(c == Short.class){
      return new ShortTypeProvider((Short)o);
    }
    
    if(c == String.class){
      return new StringTypeProvider((String)o);
    }
    
    if(c == int[].class){
      return new IntArrayTypeProvider((int[])o);
    }
    
    if(c == float[].class){
      return new FloatArrayTypeProvider((float[])o);
    }
    
    if(List.class.isAssignableFrom(c)){
      List<?> list = (List<?>) o;
      if(list.isEmpty()){
        return new FloatArrayTypeProvider(new float[]{});
      }
      Object first = list.get(0);
      if(first.getClass() == Integer.class){
        int[] arr = new int[list.size()];
        int i = 0;
        for(Object x : list){
          arr[i++] = ((Integer) x);
        }
        return new IntArrayTypeProvider(arr);
      }
      
      if(first.getClass() == Float.class){
        float[] arr = new float[list.size()];
        int i = 0;
        for(Object x : list){
          arr[i++] = (Float) x;
        }
        return new FloatArrayTypeProvider(arr);
      }
      
      if(first.getClass() == Double.class){
        float[] arr = new float[list.size()];
        int i = 0;
        for(Object x : list){
          arr[i++] = (float)((Double) x).doubleValue();
        }
        return new FloatArrayTypeProvider(arr);
      }
    }
    
    
    
    return NothingProvider.INSTANCE;
    
  }

}
