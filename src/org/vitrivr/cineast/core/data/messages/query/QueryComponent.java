package org.vitrivr.cineast.core.data.messages.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.vitrivr.cineast.core.data.query.containers.QueryContainer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
     * Constructor for QueryComponent.
     *
     * @param terms
     */
    @JsonCreator
    public QueryComponent(@JsonProperty("terms") QueryTerm[] terms) {
        this.terms = terms;
    }

    /**
     * Getter for terms.
     *
     * @return List of QueryTerms
     */
    public List<QueryTerm> getTerms() {
        if (this.terms != null) {
            return Arrays.asList(this.terms);
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * Converts the provided collection of QueryComponent objects to a map that maps feature categories
     * defined in the query-terms to @{@link QueryContainer} derived from the {@link QueryTerm}.
     *
     * @return Category map.
     */
    public static HashMap<String, ArrayList<QueryContainer>> toCategoryMap(Collection<QueryComponent> components) {
        final HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
        for (QueryComponent component : components) {
            for (QueryTerm term : component.getTerms()) {
                for (String category : term.getCategories()) {
                    if (!categoryMap.containsKey(category)) {
                        categoryMap.put(category, new ArrayList<>());
                    }
                    final QueryContainer container = term.toContainer();
                    if (container != null) {
                      categoryMap.get(category).add(container);
                    }
                }
            }
        }
        return categoryMap;
    }
}
