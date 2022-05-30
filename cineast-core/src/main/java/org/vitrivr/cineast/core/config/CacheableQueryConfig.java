package org.vitrivr.cineast.core.config;

public class CacheableQueryConfig extends ReadableQueryConfig {

  public CacheableQueryConfig(ReadableQueryConfig qc) {
    super(qc, "cached");
  }

}
