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
    private QueryTerm[] terms = new QueryTerm[1];

    /**
     * Instance of the ImageQueryTerm for this QueryComponent. Can be NULL!
     */
    private final QueryTermImage queryTermImage;

    /**
     *
     * @param queryTermImage
     */
    @JsonCreator
    public QueryComponent(@JsonProperty("imageQueryTerm") QueryTermImage queryTermImage) {
        this.queryTermImage = queryTermImage;

        /* Add query-terms to the list. */
        if (this.queryTermImage !=null) this.terms[0] = this.queryTermImage;
    }

    /**
     *
     * @return
     */
    public List<QueryTerm> getTerms() {
        return Arrays.asList(this.terms);
    }
}
