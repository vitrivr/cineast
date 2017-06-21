package org.vitrivr.cineast.core.data.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MetadataLookup implements Message {
    /**
     *
     */
    private String[] objectIds;

    /**
     *
     */
    private String[] domains;

    /**
     *
     * @param objectids
     * @param domains
     */
    @JsonCreator
    public MetadataLookup(@JsonProperty("objectIds") String[] objectids, @JsonProperty("domains") String[] domains) {
        this.objectIds = objectids;
        this.domains = domains;
    }

    /**
     *
     * @return
     */
    public List<String> getObjectids() {
        if (this.objectIds != null) {
            return Arrays.asList(this.objectIds);
        } else {
            return new ArrayList<>(0);

        }
    }

    /**
     *
     * @return
     */
    public  List<String> getDomains() {
        if (this.domains != null) {
            return Arrays.asList(this.domains);
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageTypes getMessagetype() {
        return null;
    }
}
