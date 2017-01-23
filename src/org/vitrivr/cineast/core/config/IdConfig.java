package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.HashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class IdConfig {

    public enum ExistenceCheck {
        NOCHECK,
        CHECK_SKIP,
        CHECK_PROCEED
    }

    /** Name of the ObjectIdGenerator. Must correspond to the simple-name or the FQN of the respective class.
     *
     * @see ObjectIdGenerator
     */
    private String name = "UniqueObjectIdGenerator";

    /** Properties that are being used to initialize the ObjectIdGenerator.
     *
     * @see ObjectIdGenerator
     */
    private HashMap<String, String> properties = new HashMap<>();

    /** Determines the 'CheckMode' for objectId's of MediaObjectDescriptors, i.e. whether their uniqueness should be explicitly checked and
     * what the consequences of the a collision should be.
     *
     * NOCHECK      = No check will be performed. If a ID happens to collide a DB error will be raised.
     * CHECK_SKIP   = Checks the uniqueness of an ID. If it's not unique, that item is skipped.
     * CHECK_SKIP   = Checks the uniqueness of an ID. If it's not unique, that item is still processed but no new MediaObjectDescriptor is created.
     */
    private ExistenceCheck existenceCheckMode = ExistenceCheck.NOCHECK;

    @JsonProperty
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public HashMap<String, String> getProperties() {
        return properties;
    }
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    @JsonProperty
    public ExistenceCheck getExistenceCheckMode() {
        return existenceCheckMode;
    }
    public void setExistenceCheckMode(ExistenceCheck existenceCheckMode) {
        this.existenceCheckMode = existenceCheckMode;
    }

    @JsonIgnore
    public ObjectIdGenerator getGenerator() {
        ObjectIdGenerator generator = ReflectionHelper.newIdGenerator(this.name);
        generator.init(this.properties);
        return generator;
    }
}
