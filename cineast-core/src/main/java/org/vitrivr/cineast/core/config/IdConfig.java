package org.vitrivr.cineast.core.config;

import java.util.HashMap;
import java.util.Map;

import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.util.ReflectionHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public final class IdConfig {

    public enum ExistenceCheck {
        CHECK_SKIP,
        CHECK_PROCEED
    }

    /**
     * Name of the ObjectIdGenerator. Must correspond to the simple-name or the FQN of the respective class.
     *
     * @see ObjectIdGenerator
     */
    private final String name;

    /**
     * Properties that are being used to initialize the ObjectIdGenerator.
     *
     * @see ObjectIdGenerator
     */
    private final Map<String, String> properties;

    /**
     * Determines the 'existence check mode' for objectId's of {@link MediaObjectDescriptor}s, i.e. whether their uniqueness should be explicitly
     * checked and what the consequences of the a collision should be.
     *
     * CHECK_SKIP  = Checks the uniqueness of an ID. If it's not unique, that item is skipped.
     * CHECK_PROCEED  = Checks the uniqueness of an ID. If it's not unique, that item is still processed but no new descriptor is created.
     */
    private final ExistenceCheck existenceCheckMode;

    /**
     * Constructor for default {@link IdConfig}.
     */
    public IdConfig() {
        this("UniqueObjectIdGenerator", ExistenceCheck.CHECK_SKIP, new HashMap<>());
    }


    /**
     * Constructor for {@link IdConfig}. Used for deserialization of a {@link IdConfig} instance from a configuration file.
     *
     * @param name Name of the {@link ObjectIdGenerator}
     * @param existenceCheckMode  Determines the 'existence check mode' for objectId's of {@link MediaObjectDescriptor}s
     * @param properties
     */
    @JsonCreator
    public IdConfig(@JsonProperty(value = "name", required = true) String name,
                    @JsonProperty(value = "existenceCheckMode") ExistenceCheck existenceCheckMode,
                    @JsonProperty(value = "properties") Map<String,String> properties) {

        this.name = name;
        this.existenceCheckMode = (existenceCheckMode == null ? ExistenceCheck.CHECK_SKIP : existenceCheckMode);
        this.properties = (properties == null ? new HashMap<>(0) : properties);
    }


    public String getName() {
        return this.name;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public ExistenceCheck getExistenceCheckMode() {
        return this.existenceCheckMode;
    }

    /**
     * Returns a new {@link ObjectIdGenerator} based on this {@link IdConfig} or null, if the new {@link ObjectIdGenerator} could not be created.
     *
     * @return New {@link ObjectIdGenerator} or null.
     */
    @JsonIgnore
    public ObjectIdGenerator getGenerator() {
        return ReflectionHelper.newIdGenerator(this.name, this.properties);
    }
}
