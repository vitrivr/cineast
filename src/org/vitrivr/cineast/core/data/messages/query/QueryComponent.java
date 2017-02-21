package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryComponent {
    /**
     * List of QueryTerm objects for this QueryComponent. This is for convenience only.
     */
    private final QueryTerm[] terms;

    /**
     *
     * @param terms
     */
    @JsonCreator
    public QueryComponent(@JsonProperty("terms") QueryTerm[] terms) {
        this.terms = terms;
    }

    /**
     *
     * @return
     */
    public List<QueryTerm> getTerms() {
        return Arrays.asList(this.terms);
    }
}
