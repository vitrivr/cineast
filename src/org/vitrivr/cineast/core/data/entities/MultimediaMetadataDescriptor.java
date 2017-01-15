package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.providers.primitive.*;


/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
/* TODO: Review with Luca. */
public class MultimediaMetadataDescriptor {
    /** Name of the entity in the persistence layer. */
    public static final String ENTITY = "cineast_metadata";

    /** Field names in the persistence layer. */
    public static final String[] FIELDNAMES = {"id", "mediatype", "name", "path", "preview", "segments"};

    /*
     * ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to.
     */
    private final String objectId;

    /*
     * Key (name) of the metadata entry. Must NOT be unique for a given object.
     */
    private final String key;

    /*
     * Value of the MetadataDescriptor.
     */
    private final PrimitiveTypeProvider value;

    /**
     * Constructor for MultimediaMetadataDescriptor. Tries to infer the type of the provided value by means of
     * instance of. If the value is not compatible with the default primitive types, the object's toString() method is
     * used to get a String representation.
     *
     * @param objectId ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to.
     * @param key Key (name) of the metadata entry.
     * @param value Value of the metadata entry. Can be any type of object, but only Double, Float, Int, Long and String are supported officialy.
     */
    public MultimediaMetadataDescriptor(String objectId, String key, Object value) {
        this.objectId = objectId;
        this.key = key;
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
    }
    
    @JsonProperty
    public String getObjectId() {
        return objectId;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public Object getValue() {
        return PrimitiveTypeProvider.getObject(this.value);
    }
}

