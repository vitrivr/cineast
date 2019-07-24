package org.vitrivr.cineast.core.extraction.idgenerator;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.RandomStringGenerator;

/**
 * Generates an objectId from a random string.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class UniqueObjectIdGenerator implements ObjectIdGenerator {
    /** Property-name for a custom start value (can be set in the configuration). */
    private static final String PROPERTY_LENGTH_KEY = "length";

    /** Property-name for a custom start value (can be set in the configuration). */
    private static final Integer PROPERTY_LENGTH_DEFAULT = 16;

    /** Length of the generated ID. */
    private int length = 16;
    
    private HashSet<String> usedIds = new HashSet<>();

    /**
     * Constructor for {@link UniqueObjectIdGenerator}.
     */
    public UniqueObjectIdGenerator() {
        this.length = PROPERTY_LENGTH_DEFAULT;
    }

    /**
     * Constructor for {@link UniqueObjectIdGenerator}.
     *
     * @param properties HashMap of named parameters. The values 'start' and 'format' are supported parameter keys.
     */
    public UniqueObjectIdGenerator(Map<String,String> properties) {
        this.length = Integer.parseInt(properties.getOrDefault(PROPERTY_LENGTH_KEY, PROPERTY_LENGTH_DEFAULT.toString()));
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
        String rawId;
        do {
            rawId = RandomStringGenerator.generateRandomString(this.length);
        } while(this.usedIds.contains(rawId));
        this.usedIds.add(rawId);
        return MediaType.generateId(type, rawId);
    }
}
