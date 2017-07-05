package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This object represents a similarity-query message, i.e. a request for a similarity-search.
 *
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class SimilarityQuery implements Message {
    /** List of QueryComponents that are part of this similarity-query. */
    private QueryComponent[] components;

    /** List of MediaTypes that should be considered when executing the query. */
    private MediaType[] types;

    /**
     * Constructor for the SimilarityQuery object.
     *
     * @param components List of query components.
     * @param types List of MediaTypes.
     */
    @JsonCreator
    public SimilarityQuery(@JsonProperty("containers") QueryComponent[] components, @JsonProperty("types") MediaType[] types) {
        this.components = components;
        this.types = types;
    }

    /**
     * Getter for containers.
     *
     * @return
     */
    public List<QueryComponent> getComponents() {
        if (this.components != null) {
            return Arrays.asList(this.components);
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
        return MessageType.Q_SIM;
    }
}
