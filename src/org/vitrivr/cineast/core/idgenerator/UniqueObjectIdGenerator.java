package org.vitrivr.cineast.core.idgenerator;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.RandomStringGenerator;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Generates an objectId from a random string.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class UniqueObjectIdGenerator implements ObjectIdGenerator {

    /** Property-name for a custom start value (can be set in the configuration). */
    private static final String PROPERTY_NAME_LENGTH = "length";

    /** Length of the generated ID. */
    private int length = 16;

    /**
     * Can be used to initialize a particular ObjectIdGenerator instance by passing
     * a HashMap of named parameters.
     *
     * 'length' is supported parameter.
     *
     * @param properties HashMap of named paramters.
     */
    @Override
    public void init(HashMap<String, String> properties) {
        String length = properties.get(PROPERTY_NAME_LENGTH);
        if (length != null) this.length = Integer.parseInt(length);
    }

    /**
     * Generates the next ID in the sequence.
     *
     * @param path Path to the file for which an ID should be generated.
     * @param type MediaType of the file for which an ID should be generated.
     * @return
     */
    @Override
    public String next(Path path, MediaType type) {
        String rawId = RandomStringGenerator.generateRandomString(this.length);
        return MediaType.generateId(type, rawId);
    }
}
