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

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MetadataLookup implements Message {
    /** List of object ID's for which metadata should be looked up. */
    private String[] objectIds;

    /** List of metadata domains that should be considered. If empty, all domains are considered! */
    private String[] domains;

    @JsonCreator
    public MetadataLookup(@JsonProperty("objectids") String[] ids, @JsonProperty("domains") String[] domains) {
        this.objectIds = ids;
        this.domains = domains;
    }

    public List<String> getIds() {
        if (this.objectIds != null) {
            return Arrays.asList(this.objectIds);
        } else {
            return new ArrayList<>(0);
        }
    }


    public  List<String> getDomains() {
        if (this.domains != null) {
            return Arrays.asList(this.domains);
        } else {
            return new ArrayList<>(0);
        }
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.M_LOOKUP;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
