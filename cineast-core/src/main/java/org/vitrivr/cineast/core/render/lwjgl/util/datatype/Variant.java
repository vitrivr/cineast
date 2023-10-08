package org.vitrivr.cineast.core.render.lwjgl.util.datatype;

import java.util.HashMap;
import java.util.Map;

/**
 * A variant class that can hold any type of object To store an object in the variant use the set method and the generic identifier of the type e.g. variant.<Model>set("name", model); To retrieve an object from the variant use the get method and the generic identifier of the type e.g. var model = variant.<Model>get("name"); The variant class can also be merged with other variants.
 */
public class Variant {

  private final Map<String, Object> variants;

  public Variant() {
    this.variants = new HashMap<>();
  }


  /**
   * Add a value of type T to the variant If T is a variant, all values of the variant are added to the current variant
   *
   * @param key   Key of the value If the key already exists, an exception is thrown
   * @param value Value to add
   */
  public <T> Variant set(String key, T value) {
    try {
      if (value instanceof Variant) {
        this.variants.putAll(((Variant) value).variants);
      } else {
        this.variants.put(key, value);
      }
    } catch (IllegalArgumentException ex) {
      throw new VariantException("Key already exists");
    }
    return this;
  }

  /**
   * Get the value stored under the given key from the variant.
   * If the key does not exist, an exception is thrown
   * If the type of the value does not match the type of T, an exception is thrown
   */
  public <T> T get(Class<T> clazz, String key) throws VariantException {
    var val = this.variants.get(key);
    T result;
    try {
      result = clazz.cast(val);
    } catch (Exception ex) {
      throw new VariantException("type mismatch" + val.getClass(), ex);
    }
    return result;
  }

  /**
   * Get the value stored under the given key from the variant and remove it from the variant.
   * If the key does not exist, an exception is thrown
   * If the type of the value does not match the type of T, an exception is thrown
   */
  public <T> T remove(Class<T> clazz, String key) throws VariantException {
    var val = this.variants.remove(key);
    T result;
    try {
      result = clazz.cast(val);
    } catch (ClassCastException ex) {
      throw new VariantException("type mismatch");
    }
    return result;
  }

  /**
   * Clears the variant
   */
  public void clear() {
    this.variants.clear();
  }
}
