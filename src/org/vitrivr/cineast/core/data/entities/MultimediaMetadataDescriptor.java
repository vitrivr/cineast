package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.providers.primitive.*;
import org.vitrivr.cineast.core.db.dao.reader.DatabaseLookupException;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaMetadataReader;

import java.util.Map;


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

    /** ID of the MultimediaObject this MultimediaMetadataDescriptor belongs to. */
    private final String objectId;

    /** String value that identifies the metadata domain (e.g. EXIF, IPTC, DC...) */
    private final String domain;

    /** Key (name) of the metadata entry. Must NOT be unique for a given object. */
    private final String key;

    /** Value of the MetadataDescriptor. */
    private final PrimitiveTypeProvider value;

    /** Flag that indicates whether or not the MultimediaMetadataDescriptor has been stored persistently in the underlying. */
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

    /**
     * Constructor for MultimediaMetadataDescriptor which can be used to create a MultimediaMetadataDescriptor from a Map
     * containing the fieldnames as keys and the PrimitiveTypeProviders as value. Maps like this are usually returned
     * by DB lookup classes.
     *
     * @see PrimitiveTypeProvider
     * @see MultimediaMetadataReader
     *
     * @param data Map that maps the fieldnames to PrimitiveTypeProvider's.
     * @throws DatabaseLookupException If a required field could not be mapped.
     */
    public MultimediaMetadataDescriptor(Map<String, PrimitiveTypeProvider> data) throws DatabaseLookupException {
        if (data.get(FIELDNAMES[0])!= null && data.get(FIELDNAMES[0]).getType() == ProviderDataType.STRING) {
            this.objectId = data.get(FIELDNAMES[0]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[0] + "' for MultimediaObjectDescriptor.");
        }

        if (data.get(FIELDNAMES[1])!= null && data.get(FIELDNAMES[1]).getType() == ProviderDataType.STRING) {
            this.domain = data.get(FIELDNAMES[1]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[1] + "' for MultimediaObjectDescriptor.");
        }

        if (data.get(FIELDNAMES[2])!= null && data.get(FIELDNAMES[2]).getType() == ProviderDataType.STRING) {
            this.key = data.get(FIELDNAMES[2]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[2] + "' for MultimediaObjectDescriptor.");
        }

        this.value = data.get(FIELDNAMES[3]);
        this.exists = true;
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

