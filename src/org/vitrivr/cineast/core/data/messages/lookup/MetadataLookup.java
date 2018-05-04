package org.vitrivr.cineast.core.data.messages.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
