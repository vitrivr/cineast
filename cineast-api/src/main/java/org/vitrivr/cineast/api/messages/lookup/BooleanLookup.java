package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooleanLookup implements Message {


    /**
     * List of object ID's for which metadata should be looked up.
     */
    private String table_name;

    private String attribute;

    private String value;


    /**
     * List of metadata domains that should be considered. If empty, all domains are considered!
     */

    /**
     * Constructor for the MetadataLookup object.
     *
     * @param ids     Array of String object IDs to be looked up.
     * @param domains Array of String of the metadata domains to be considered.
     */
    @JsonCreator
    public BooleanLookup(@JsonProperty("table_name") String table,@JsonProperty("attribute") String
     attribute, @JsonProperty("value") String value) {
        this.table_name = table;
        this.attribute = attribute;
        this.value = value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.M_LOOKUP;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

    public String getTable_name() {
        return table_name;
    }
    public String getAttribute() {
        return attribute;
    }
    public String getValue() {
        return value;
    }
}
