package org.vitrivr.cineast.core.data.entities;

import static org.vitrivr.cineast.core.util.CineastConstants.SEGMENT_ID_COLUMN_QUALIFIER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.Charset;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.ExistenceCheck;
import org.vitrivr.cineast.core.data.providers.primitive.NothingProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.providers.primitive.StringProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.DatabaseLookupException;

public class MediaSegmentMetadataDescriptor implements ExistenceCheck {

    /**
     * Name of the entity in the persistence layer.
     */
    public static final String ENTITY = "cineast_segmentmetadata";
    /**
     * Field names in the persistence layer.
     */
    public static final String[] FIELDNAMES = {SEGMENT_ID_COLUMN_QUALIFIER, "domain", "key", "value"};
    private static final Logger LOGGER = LogManager.getLogger();
    private final String segmentId;

    /**
     * String value that identifies the metadata domain (e.g. EXIF, IPTC, DC...)
     */
    private final String domain;

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
     * analogous to {@link MediaObjectMetadataDescriptor}
     */
    @JsonCreator
    public MediaSegmentMetadataDescriptor(@JsonProperty(value = "segmentId", defaultValue = "") String segmentId, @JsonProperty("domain") String domain, @JsonProperty("key") String key, @JsonProperty("value") @Nullable Object value, @JsonProperty(value = "exists", defaultValue = "false") boolean exists) {
        this.segmentId = segmentId;
        this.key = key;
        this.domain = domain;
        this.exists = exists;

        outer:
        if (value != null & MediaObjectMetadataDescriptor.isSupportedValue(value)) {
            this.value = new StringTypeProvider(value.toString());
        } else {
            if (value instanceof StringProvider) {
                this.value = new StringTypeProvider(((StringProvider) value).getString());
                break outer;
            }
            if (value instanceof com.drew.metadata.StringValue) {
                this.value = new StringTypeProvider(((com.drew.metadata.StringValue) value).toString(Charset.defaultCharset()));
            } else {
                LOGGER.warn("Value type {} not supported, value is {} for key {}", value.getClass().getSimpleName(), value.toString(), key);
                this.value = new NothingProvider();
            }
        }
    }

    /**
     * analogous to {@link MediaObjectMetadataDescriptor}
     */
    public MediaSegmentMetadataDescriptor(Map<String, PrimitiveTypeProvider> data) throws DatabaseLookupException {
        if (data.get(FIELDNAMES[0]) != null && data.get(FIELDNAMES[0]).getType() == ProviderDataType.STRING) {
            this.segmentId = data.get(FIELDNAMES[0]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[0] + "' for MediaSegmentMetadataDescriptor.");
        }

        if (data.get(FIELDNAMES[1]) != null && data.get(FIELDNAMES[1]).getType() == ProviderDataType.STRING) {
            this.domain = data.get(FIELDNAMES[1]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[1] + "' for MediaSegmentMetadataDescriptor.");
        }

        if (data.get(FIELDNAMES[2]) != null && data.get(FIELDNAMES[2]).getType() == ProviderDataType.STRING) {
            this.key = data.get(FIELDNAMES[2]).getString();
        } else {
            throw new DatabaseLookupException("Could not read column '" + FIELDNAMES[2] + "' for MediaSegmentMetadataDescriptor.");
        }

        this.value = data.get(FIELDNAMES[3]);
        this.exists = true;
    }

    public static MediaSegmentMetadataDescriptor of(String segmentId, String domain, String key, @Nullable Object value) {
        return new MediaSegmentMetadataDescriptor(segmentId, domain, key, value, false);
    }

    @JsonProperty
    public String getSegmentId() {
        return this.segmentId;
    }

    @JsonProperty
    public String getDomain() {
        return this.domain;
    }

    @JsonProperty
    public String getKey() {
        return this.key;
    }

    @JsonProperty
    public String getValue() {
        return this.value.getString();
    }

    @JsonIgnore
    public PrimitiveTypeProvider getValueProvider() {
        return this.value;
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
