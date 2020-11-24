package org.vitrivr.cineast.core.db.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstraction of entity definition regardless of the database used
 */
public final class EntityDefinition {

    /**
     * The entity's name
     */
    private final String entityName;
    /**
     * Its attributes
     */
    private final List<AttributeDefinition> attributes;
    /**
     * Flag whether this entity will have at maximum one vector per segment
     */
    private final boolean oneVectorPerSegment;

    /**
     * Creates a new entity definition with given parameters
     *
     * @param entityName          The name of the entity
     * @param attributes          A list of attributes
     * @param oneVectorPerSegment Whether this entity will have one vector per segment at max
     */
    private EntityDefinition(String entityName, List<AttributeDefinition> attributes, boolean oneVectorPerSegment) {
        this.entityName = entityName;
        this.attributes = attributes;
        this.oneVectorPerSegment = oneVectorPerSegment;
    }

    /**
     * Returns the name of the entity
     *
     * @return The name of the entity
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns an immutable list of attributes
     *
     * @return An immutable list of attributes
     */
    public List<AttributeDefinition> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Whether this entity will have one vector per segment at max
     *
     * @return This entity will have one vector per segment at max
     */
    public boolean isOneVectorPerSegment() {
        return oneVectorPerSegment;
    }

    @Override
    public String toString() {
        return "EntityDefinition{" +
                "entityName='" + entityName + '\'' +
                ", attributes=" + attributes +
                ", oneVectorPerSegment=" + oneVectorPerSegment +
                '}';
    }

    /**
     * A builder for {@link EntityDefinition}s
     */
    public static class EntityDefinitionBuilder {
        private String name;
        private List<AttributeDefinition> attrs = new ArrayList<>();
        private boolean oneVectorPerSegment;

        /**
         * Creates a new builder for a named entity
         *
         * @param name The name of the entity
         */
        public EntityDefinitionBuilder(String name) {
            this.name = name;
        }

        /**
         * Creates a new builder from an entity defintiion, to modify said one
         * @param def The defintiion template for this builder
         * @return A new builder
         */
        public static EntityDefinitionBuilder from(EntityDefinition def) {
            EntityDefinitionBuilder builder = new EntityDefinitionBuilder(def.getEntityName());
            builder.attrs.addAll(def.getAttributes());
            builder.oneVectorPerSegment = def.oneVectorPerSegment;
            return builder;
        }

        public void reset(){
            name = "";
            attrs.clear();
            oneVectorPerSegment = false;
        }

        /**
         * Sets the name of the entity to build
         *
         * @param name The name of the entity
         * @return The builder
         */
        public EntityDefinitionBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the flag whether the entity has at max one vector per segment
         *
         * @return The builder
         */
        public EntityDefinitionBuilder producesOneVectorPerSegment() {
            this.oneVectorPerSegment = true;
            return this;
        }

        /**
         * Adds an attribute to the entity
         *
         * @param attr The attribute to add
         * @return The builder
         */
        public EntityDefinitionBuilder withAttribute(AttributeDefinition attr) {
            this.attrs.add(attr);
            return this;
        }

        /**
         * Adds a vector attribute with a given name and specified length
         *
         * @param featureName The name of the feature
         * @param length      The length of the vector
         * @return The builder
         */
        public EntityDefinitionBuilder withAttribute(String featureName, int length) {
            this.attrs.add(new AttributeDefinition(featureName, AttributeDefinition.AttributeType.VECTOR, length));
            return this;
        }

        /**
         * Adds all given attributes
         *
         * @param attributes The attributes to add
         * @return The builder
         */
        public EntityDefinitionBuilder withAttributes(AttributeDefinition... attributes) {
            this.attrs.addAll(Arrays.asList(attributes));
            return this;
        }

        /**
         * Adds many named vector attributes with the same length
         *
         * @param length       The length of the vectors
         * @param featureNames the names
         * @return The builder
         */
        public EntityDefinitionBuilder withAttributes(int length, String... featureNames) {
            Arrays.stream(featureNames).forEach(s -> withAttribute(s, length));
            return this;
        }

        /**
         * Adds a special 'id' attribute of type {@link AttributeDefinition.AttributeType#STRING}
         *
         * @return the builder
         */
        public EntityDefinitionBuilder withIdAttribute() {
            // TODO add hash hint
            this.attrs.add(0, new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING));
            return this;
        }

        /**
         * Builds the entity definition
         *
         * @return The entity definition
         */
        public EntityDefinition build() {
            // First sort so that id column is first
            attrs.sort((o1, o2) -> {
                if (o1.getName().equals("id")) {
                    return Integer.MIN_VALUE;
                } else if (o2.getName().equals("id")) {
                    return Integer.MAX_VALUE;
                } else {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            return new EntityDefinition(name, attrs, oneVectorPerSegment);
        }
    }
}
