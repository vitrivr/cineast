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
    private String[] ids;

    /** List of metadata domains that should be considered. If empty, all domains are considered! */
    private String[] domains;

    /**
     * Default constructor.
     *
     * @param objectids
     * @param domains
     */
    @JsonCreator
    public MetadataLookup(@JsonProperty("ids") String[] ids, @JsonProperty("domains") String[] domains) {
        this.ids = ids;
        this.domains = domains;
    }

    /**
     *
     * @return
     */
    public List<String> getObjectids() {
        if (this.ids != null) {
            return Arrays.asList(this.ids);
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
    public MessageType getMessageType() {
        return MessageType.M_LOOKUP;
    }
}
