package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.setup.AttributeDefinition;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.01.17
 */
/* TODO: Review with Luca. */
public class MultimediaMetadataDescriptor {
    /*
     * ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to.
     */
    private final String objectId;

    /*
     * Type of the MetadataDescriptor. Corresponds to the type of the value property that has been set.
     */
    private final String type;

    /*
     * Key (name) of the metadata entry. Must NOT be unique for a given object.
     */
    private final String key;

    /*
     * Value properties for the different datatypes.
     */
    private String str_value;
    private Float flt_value;
    private Double dbl_value;
    private Integer int_value;
    private Long lng_value;

    /**
     * Constructor for MultimediaMetadataDescriptor. Tries to infer the type of the provided value by means of
     * instance of. If the value is not compatible with the default types, the object's toString() method is
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
            this.flt_value = (Float) value;
            this.type = AttributeDefinition.AttributeType.DOUBLE.toString();
        } else if (value instanceof Double) {
            this.dbl_value = (Double) value;
            this.type = AttributeDefinition.AttributeType.FLOAT.toString();
        } else if (value instanceof Integer) {
            this.int_value = (Integer) value;
            this.type = AttributeDefinition.AttributeType.INT.toString();
        } else if (value instanceof Long) {
            this.lng_value = (Long)value;
            this.type = AttributeDefinition.AttributeType.LONG.toString();
        } else if (value instanceof String) {
            this.str_value = (String) value;
            this.type = AttributeDefinition.AttributeType.STRING.toString();
        } else if (value != null) {
            this.str_value = value.toString();
            this.type = AttributeDefinition.AttributeType.STRING.toString();
        } else {
            this.type = "EMPTY";
        }
    }

    /**
     *
     * @return
     */
    @JsonProperty
    public String getObjectId() {
        return objectId;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public String getType() {
        return type;
    }

    @JsonProperty
    public String getStringValue() {
        return str_value;
    }

    @JsonProperty
    public Double getDoubleValue() {
        return dbl_value;
    }

    @JsonProperty
    public Float getFloatValue() {
        return flt_value;
    }

    @JsonProperty
    public Integer getIntegerValue() {
        return int_value;
    }

    @JsonProperty
    public Long getLongValue() {
        return lng_value;
    }
}

