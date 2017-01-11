package org.vitrivr.cineast.core.data.queries;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class Query {
    private QueryComponent[] components;

    /**
     *
     * @param components
     */
    @JsonCreator
    public Query(@JsonProperty("components")QueryComponent[] components) {
        this.components = components;
    }

    /**
     * Getter for components.
     *
     * @return
     */
    public List<QueryComponent> getComponents() {
        return Arrays.asList(this.components);
    }
}
