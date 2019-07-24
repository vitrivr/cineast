package org.vitrivr.cineast.core.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap which returns a default value in case the key is unknown
 */
public class DefaultValueHashMap<K, V> extends HashMap<K, V> {

  private static final long serialVersionUID = -8326007409332438019L;

  private static final Logger LOGGER = LogManager.getLogger();
  
  private final V defaultValue;
  
  public DefaultValueHashMap(V defaultValue) {
    super();
    this.defaultValue = defaultValue;
  }

  public DefaultValueHashMap(int initialCapacity, float loadFactor, V defaultValue) {
    super(initialCapacity, loadFactor);
    this.defaultValue = defaultValue;
  }

  public DefaultValueHashMap(int initialCapacity, V defaultValue) {
    super(initialCapacity);
    this.defaultValue = defaultValue;
  }

  public DefaultValueHashMap(Map<? extends K, ? extends V> m, V defaultValue) {
    super(m);
    this.defaultValue = defaultValue;
  }

  @Override
  public V get(Object key) {
    if(containsKey(key)){
      return super.get(key);
    }
    LOGGER.info("key {} not present, returning default value {}", key, defaultValue);
    return defaultValue;
  }

  
  
}
