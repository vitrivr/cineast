package org.vitrivr.cineast.core.idgenerator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.vitrivr.cineast.core.data.MediaType;

/**
 * Generates objectIds from a counter that is incremented every time a new ID is generated.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class SequentialObjectIdGenerator implements ObjectIdGenerator {

    /** Property-name for a custom start value (can be set in the configuration). */
    private static final String PROPERTY_NAME_START = "start";

    /** Property-name for a custom format (can be set  in the configuration). */
    private static final String PROPERTY_NAME_FORMAT = "format";

    /** Internal counter used to keep track of the ID's. */
    private final AtomicLong counter = new AtomicLong(1);

    /** String format used to generate a String representation of the incremental ID. */
    private String format = "%07d";

    /**
     * Can be used to initialize a particular SequentialObjectIdGenerator instance by passing
     * a HashMap of named parameters.
     *
     * 'start' and 'format' are supported parameters.
     *
     *
     * @param properties HashMap of named parameters.
     */
    @Override
    public void init(HashMap<String, String> properties) {
        String start = properties.get(PROPERTY_NAME_START);
        if (start != null) {
          this.counter.set(Long.parseLong(start));
        }

        String format = properties.get(PROPERTY_NAME_FORMAT);
        if (format != null) {
          this.format = format;
        }
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
        return MediaType.generateId(type, String.format(this.format,this.counter.getAndIncrement()));
    }
}
