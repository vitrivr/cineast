package org.vitrivr.cineast.core.util.json;

/**
 * Wraps the Object to JSON serialization so as to make sure that it can be provided independently of a concrete library.
 */
public interface JsonWriter {

  String JSON_EMPTY = "{}";

  /**
   * Takes a Java Object (usually a POJO) and tries to serialize it into a JSON. If serialization fails for some reason, this method should return JSON_EMPTY;
   */
  String toJson(Object object);
}
