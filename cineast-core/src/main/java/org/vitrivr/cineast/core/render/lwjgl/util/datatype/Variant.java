package org.vitrivr.cineast.core.render.lwjgl.util.datatype;

import java.util.HashMap;
import java.util.Map;

public class Variant {

  private final Map<String, Object> variants;

  public Variant() {
    this.variants = new HashMap<>();
  }


  public <T> Variant set(String key, T value) {
    var type = value.getClass();
    if (value instanceof Variant) {
      ((Variant) value).variants.forEach((k, v) -> this.variants.put(k, v));
    }
    this.variants.put(key, (Object) value);
    return this;
  }

  public <T> T get(Class<T> clazz , String key) throws VariantException {
    var val = this.variants.get(key);
    T result = null;
    try {
      result = clazz.cast(val);
    } catch (Exception ex) {
      throw new VariantException("type mismatch" + val.getClass(), ex);
    }
    return result;
  }


  public <T> T remove(Class<T> clazz, String key) throws VariantException{
    var val = this.variants.remove(key);
    T result = null;
    try{
      result = clazz.cast(val);
    } catch (ClassCastException ex) {
      throw new VariantException("type mismatch");
    }
    return result;
  }

  public void clear(){
    this.variants.clear();
  }
}
