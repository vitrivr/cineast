package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.providers.primitive.*;


/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
public class MultimediaMetadataDescriptor implements ExistenceCheck {
    /** Name of the entity in the persistence layer. */
    public static final String ENTITY = "cineast_metadata";

    /** Field names in the persistence layer. */
    public static final String[] FIELDNAMES = {"objectid", "domain", "key", "value"};

    /** ID of the MultimediaMetadataDescriptor. */
    private String metadataId;

    /** ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to. */
    private final String objectId;

    /** */
    private final String domain;

    /** Key (name) of the metadata entry. Must NOT be unique for a given object. */
    private final String key;

    /** Value of the MetadataDescriptor. */
    private final PrimitiveTypeProvider value;

    /** */
    private final boolean exists;

    /**
     * Convenience method to create a MultimediaMetadataDescriptor marked as new. The method will assign
     * a new ID to this MultimediaObjectDescriptor.
     *
     * @param objectId The Path that points to the file for which a new MultimediaObjectDescriptor should be created.
     * @param key
     * @param value
     * @return A new MultimediaMetadataDescriptor
     */
    public static MultimediaMetadataDescriptor newMultimediaMetadataDescriptor(String objectId, String domain, String key, Object value) {
        return new MultimediaMetadataDescriptor(objectId, domain, key, value, false);
    }

    /**
     * Constructor for MultimediaMetadataDescriptor. Tries to infer the type of the provided value by means of
     * instance of. If the value is not compatible with the default primitive types, the object's toString() method is
     * used to get a String representation.
     *
     * @param objectId ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to.
     * @param domain
     * @param key Key (name) of the metadata entry.
     * @param value Value of the metadata entry. Can be any type of object, but only Double, Float, Int, Long and String are supported officialy.
     */
    public MultimediaMetadataDescriptor(String objectId, String domain, String key, Object value, boolean exists) {
        this.metadataId = objectId + "_" + key;
        this.objectId = objectId;
        this.key = key;
        this.domain = domain;
        if (value instanceof Float) {
            this.value = new FloatTypeProvider((Float)value);
        } else if (value instanceof Double) {
            this.value = new DoubleTypeProvider((Double) value);
        } else if (value instanceof Integer) {
            this.value = new IntTypeProvider((Integer) value);
        } else if (value instanceof Long) {
            this.value = new LongTypeProvider((Long) value);
        } else if (value instanceof String) {
            this.value = new StringTypeProvider((String) value);
        } else if (value != null) {
            this.value = new StringTypeProvider(value.toString());
        } else {
            this.value = new NothingProvider();
        }
        this.exists = exists;
    }

    @JsonProperty
    public String getMetadataId() {
        return metadataId;
    }

    @JsonProperty
    public String getObjectId() {
        return objectId;
    }

    @JsonProperty
    public String getDomain() {
        return domain;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public Object getValue() {
        return PrimitiveTypeProvider.getObject(this.value);
    }

    @Override
    public boolean exists() {
        return this.exists;
    }
}

