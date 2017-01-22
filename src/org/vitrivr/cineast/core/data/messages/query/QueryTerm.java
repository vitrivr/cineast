package org.vitrivr.cineast.core.data.messages.query;

import org.vitrivr.cineast.core.data.QueryContainer;

import java.util.Arrays;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public abstract class QueryTerm {
    /**
     * List of categories defined as part of the query-term. This ultimately selects the feature-vectors
     * used for retrieval.
     */
    private final String[] categories;

    /**
     *
     */
    private float weight;

    /**
     *
     * @param categories
     */
    public QueryTerm(String[] categories, float weight) {
        this.categories = categories;
        this.weight = weight;
    }

    /**
     *
     * @return
     */
    public List<String> getCategories() {
        return Arrays.asList(this.categories);
    }

    /**
     *
     * @return
     */
    public float getWeight() {
        return weight;
    }

    /**
     *
     * @return
     */
    public abstract QueryContainer toContainer();
}
