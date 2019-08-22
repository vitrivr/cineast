package org.vitrivr.cineast.core.db.setup;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class AttributeDefinition {

    public enum AttributeType {
        UNKOWNAT,
        AUTO,
        LONG,
        INT,
        FLOAT,
        DOUBLE,
        STRING,
        TEXT,
        BOOLEAN,
        VECTOR,
        BITSET,
        GEOMETRY,
        GEOGRAPHY
    }

    /** The name of the {@link AttributeDefinition}. */
    private final String name;

    /**The {@link AttributeType} of the {@link AttributeDefinition}. */
    private final AttributeType type;

    /** Hints to the underlying storage engine. Those hints are highly implementation specific! l. */
    private final Map<String,String> hints;

    private final int length;

    /**
     * Constructor for {@link AttributeDefinition}
     *
     * @param name Name of the attribute.
     * @param type Type of the attribute
     */
    public AttributeDefinition(String name, AttributeType type) {
        this(name, type, new HashMap<>());
    }

    public AttributeDefinition(String name, AttributeType type, int length) {
        this(name, type, length, new HashMap<>());
    }

    /**
     * Constructor for {@link AttributeDefinition}
     *
     * @param name Name of the attribute.
     * @param type Type of the attribute.
     * @param hints Hint to the storage engine regarding the handler.
     */
    public AttributeDefinition(String name, AttributeType type, Map<String,String> hints) {
        this(name, type, -1, hints);
    }

    public AttributeDefinition(String name, AttributeType type, int length, Map<String,String> hints) {
        this.name = name;
        this.type = type;
        this.hints = hints;
        this.length = length;
    }

    /**
     * Getter for the name of the {@link AttributeDefinition}.
     *
     * @return Name of the attribute.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the type of the {@link AttributeDefinition}.
     *
     * @return Name of the attribute.
     */
    public AttributeType getType() {
        return this.type;
    }

    /**
     *
     * @return
     */
    public boolean hasHint(String hint) {
        return this.hints.containsKey(hint);
    }

    /**
     *
     * @return
     */
    public Optional<String> getHint(String hint) {
        return Optional.ofNullable(this.hints.get(hint));
    }

    /**
     *
     * @return
     */
    public String getHintOrDefault(String hint, String defaultValue) {
        return this.hints.getOrDefault(hint, defaultValue);
    }

    /**
     *
     * @return
     */
    public void ifHintPresent(String hint, Consumer<String> consumer) {
        if (this.hints.containsKey(hint)) {
            consumer.accept(this.hints.get(hint));
        }
    }

    public int getLength(){
        return this.length;
    }
}