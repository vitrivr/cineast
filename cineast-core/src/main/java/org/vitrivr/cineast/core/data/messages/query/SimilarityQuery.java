package org.vitrivr.cineast.core.data.messages.query;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This object represents a similarity-query message, i.e. a request for a similarity-search.
 *
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class SimilarityQuery extends Query {
    /** List of {@link QueryComponent}s that are part of this {@link SimilarityQuery}. */
    private List<QueryComponent> components;

    /**
     * Constructor for the SimilarityQuery object.
     *
     * @param components List of {@link QueryComponent}s.
     * @param config The {@link ReadableQueryConfig}. May be null!
     */
    @JsonCreator
    public SimilarityQuery(@JsonProperty(value = "containers", required = true) List<QueryComponent> components,
                           @JsonProperty(value = "config", required = false) QueryConfig config) {
        super(config);
        this.components = components;
    }

    /**
     * Getter for containers.
     *
     * @return
     */
    public List<QueryComponent> getComponents() {
        return this.components;
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
