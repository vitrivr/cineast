package org.vitrivr.cineast.core.data.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

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
    public String[] getObjectids() {
        return this.objectIds;
    }

    /**
     *
     * @return
     */
    public String[] getDomains() {
        return this.domains;
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return null;
    }
}
