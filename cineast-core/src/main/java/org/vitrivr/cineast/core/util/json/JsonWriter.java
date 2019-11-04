package org.vitrivr.cineast.core.util.json;

/**
 * Wraps the Object to JSON serialization so as to make sure that it can be provided
 * independently of a concrete library.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public interface JsonWriter {

    String JSON_EMPTY = "{}";

    /**
     * Takes a Java Object (usually a POJO) and tries to serialize it into a JSON. If serialization
     * fails for some reason, this method should return JSON_EMPTY;
     *
     * @param object
     * @return
     */
    String toJson(Object object);
}
