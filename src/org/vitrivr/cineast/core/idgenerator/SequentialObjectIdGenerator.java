package org.vitrivr.cineast.core.idgenerator;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.ReflectionHelper;

/**
 * Generates objectIds from a counter that is incremented every time a new ID is generated.
 *
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class SequentialObjectIdGenerator implements ObjectIdGenerator {
    /** Property-name for a custom start value (can be set in the configuration). */
    private static final String PROPERTY_START_KEY = "start";

    /** Property-name for a custom format (can be set  in the configuration). */
    private static final String PROPERTY_FORMAT_KEY = "format";

    /** Default value for the format. */
    private static final String PROPERTY_FORMAT_DEFAULT = "%07d";

    /** Internal counter used to keep track of the ID's. */
    private final AtomicLong counter = new AtomicLong(1);

    /** String format used to generate a String representation of the incremental ID. */
    private final String format;

    /**
     * Constructor for {@link JSONProvidedObjectIdGenerator}. This constructor is used by {@link ReflectionHelper}
     */
    public SequentialObjectIdGenerator() {
        this.format = PROPERTY_FORMAT_DEFAULT;
    }

    /**
     * Constructor for {@link JSONProvidedObjectIdGenerator}. This constructor is used by {@link ReflectionHelper}
     *
     * @param properties HashMap of named parameters. The values 'start' and 'format' are supported parameter keys.
     */
    public SequentialObjectIdGenerator(Map<String,String> properties) {
        String start = properties.get(PROPERTY_START_KEY);
        if (start != null) {
            this.counter.set(Long.parseLong(start));
        }
        this.format = properties.getOrDefault(PROPERTY_FORMAT_KEY, PROPERTY_FORMAT_DEFAULT);
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
