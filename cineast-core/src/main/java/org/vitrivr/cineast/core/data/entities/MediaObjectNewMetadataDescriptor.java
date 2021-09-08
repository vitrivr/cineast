package org.vitrivr.cineast.core.data.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.providers.primitive.*;
import org.vitrivr.cineast.core.db.dao.reader.DatabaseLookupException;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;



public class MediaObjectNewMetadataDescriptor implements ExistenceCheck {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Name of the entity in the persistence layer.
     */
    public static final String ENTITY = "cineast_newmetadata";

    /**
     * Field names in the persistence layer.
     */
    public static final String[] FIELDNAMES = {"id", "key", "value"};

    private static final List<Class<?>> SUPPORTED_TYPES = ImmutableList
            .of(Integer.class, Long.class, Float.class, Double.class, Short.class, Byte.class,
                    String.class);

    /**
     * ID of the MultimediaObject this MediaObjectMetadataDescriptor belongs to.
     */
    private final String id;


    /**
     * Key (name) of the metadata entry. Must NOT be unique for a given object.
     */
    private final String key;

    /**
     * Value of the MetadataDescriptor.
     */
    private final PrimitiveTypeProvider value;

    private final boolean exists;

    /**
     * Convenience method to create a MediaObjectMetadataDescriptor marked as new. The method will
     * assign a new ID to this MediaObjectDescriptor.
     *
     * @param objectId ID of the MultimediaObject this MediaObjectMetadataDescriptor belongs to.
     * @param key Key (name) of the metadata entry.
     * @param value Value of the metadata entry. Can be any type of object, but only Double, Float,
     * Int, Long and String are supported officially.
     * @return A new MediaObjectMetadataDescriptor
     */
    public static MediaObjectNewMetadataDescriptor of(String objectId, String key,
                                                   @Nullable Object value) {
        return new MediaObjectNewMetadataDescriptor(objectId, key, value, false);
    }

    /**
     * Constructor for MediaObjectNewMetadataDescriptor. Tries to infer the type of the provided value by
     * means of instance of. If the value is not compatible with the default primitive types, the
     * object's toString() method is used to get a String representation.
     *
     * @param id ID of the MultimediaObject this MediaObjectMetadataDescriptor belongs to.
     * @param key Key (name) of the metadata entry.
     * @param value Value of the metadata entry. Can be any type of object, but only Double, Float,
     * Int, Long and String are supported officially.
     */
    @JsonCreator
    public MediaObjectNewMetadataDescriptor(
            @JsonProperty(value = "id", defaultValue = "") String id,
            @JsonProperty("key") String key,
            @JsonProperty("value") @Nullable Object value,
            @JsonProperty(value = "exists", defaultValue = "false") boolean exists) {
        this.id = id;
        this.key = key;
        this.exists = exists;

        outer:
        if (value != null & isSupportedValue(value)) {
            this.value = new StringTypeProvider(value.toString());
        } else {
            if (value instanceof StringProvider) {
                this.value = new StringTypeProvider(((StringProvider) value).getString());
                break outer;
            }
            if(value instanceof com.drew.metadata.StringValue){
                this.value = new StringTypeProvider(((com.drew.metadata.StringValue) value).toString(Charset
                        .defaultCharset()));
            }
            else {
                if(value != null){
                    LOGGER.warn("Value type {} not supported, value is {} for key {}", value.getClass().getSimpleName()
                            , value.toString(), key);
                }else{
                    LOGGER.warn("Value was null");
                }
                this.value = new NothingProvider();
            }
        }
    }

    static boolean isSupportedValue(Object value) {
        return SUPPORTED_TYPES.stream().anyMatch(clazz -> clazz.isInstance(value));
    }

    /**
     * Constructor for MediaObjectMetadataDescriptor which can be used to create a
     * MediaObjectMetadataDescriptor from a Map containing the fieldnames as keys and the
     * PrimitiveTypeProviders as value. Maps like this are usually returned by DB lookup classes.
     *
     * @param data Map that maps the fieldnames to PrimitiveTypeProvider's.
     * @throws DatabaseLookupException If a required field could not be mapped.
     * @see PrimitiveTypeProvider
     * @see MediaObjectMetadataReader
     */
    public MediaObjectNewMetadataDescriptor(Map<String, PrimitiveTypeProvider> data)
            throws DatabaseLookupException {
        if (data.get(FIELDNAMES[0]) != null
                && data.get(FIELDNAMES[0]).getType() == ProviderDataType.STRING) {
            this.id = data.get(FIELDNAMES[0]).getString();
        } else {
            throw new DatabaseLookupException(
                    "Could not read column '" + FIELDNAMES[0] + "' for NewMediaObjectDescriptor.");
        }

        if (data.get(FIELDNAMES[1]) != null
                && data.get(FIELDNAMES[1]).getType() == ProviderDataType.STRING) {
            this.key = data.get(FIELDNAMES[1]).getString();
        } else {
            throw new DatabaseLookupException(
                    "Could not read column '" + FIELDNAMES[1] + "' for NewMediaObjectDescriptor.");
        }

        this.value = data.get(FIELDNAMES[2]);
        this.exists = true;
    }

    @JsonProperty
    public String getObjectId() {
        return id;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public String getValue() {
        if(this.value instanceof NothingProvider){
            return null;
        }else{
            return this.value.getString();
        }
    }

    @JsonIgnore
    public PrimitiveTypeProvider getValueProvider(){
        return this.value;
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .add("value", value)
                .toString();
    }


    public static MediaObjectNewMetadataDescriptor fromExisting(MediaObjectNewMetadataDescriptor el,
                                                             String id) {
        if (id == null) {
            LOGGER.error("No id provided for this newmetadatadescriptor");
        }
        return new MediaObjectNewMetadataDescriptor(id,
                el.key, el.value, el.exists);
    }
}

