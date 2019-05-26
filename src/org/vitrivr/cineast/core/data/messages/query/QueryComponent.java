package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryComponent {

  /**
   * List of {@link QueryTerm}s in this {@link QueryComponent}.
   */
  private final List<QueryTerm> terms;

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Constructor for QueryComponent.
   */
  @JsonCreator
  public QueryComponent(@JsonProperty("terms") List<QueryTerm> terms) {
    this.terms = terms;
  }

  /**
   * Getter for terms.
   *
   * @return List of QueryTerms
   */
  public List<QueryTerm> getTerms() {
    return this.terms;
  }

  /**
   * Converts the provided collection of QueryComponent objects to a map that maps feature categories defined in the query-terms to @{@link QueryContainer} derived from the {@link QueryTerm}.
   *
   * @return Category map.
   */
  public static HashMap<String, ArrayList<QueryContainer>> toCategoryMap(Collection<QueryComponent> components) {
    final HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
    if (components.isEmpty()) {
      LOGGER.warn("Empty components collection");
    }
    for (QueryComponent component : components) {
      if (component.getTerms().isEmpty()) {
        LOGGER.warn("No terms for component {}", component);
      }
      for (QueryTerm term : component.getTerms()) {
        if (term.getCategories().isEmpty()) {
          LOGGER.warn("No categories for term {}", term);
        }
        for (String category : term.getCategories()) {
          if (!categoryMap.containsKey(category)) {
            categoryMap.put(category, new ArrayList<>());
          }
          final QueryContainer container = term.toContainer();
          if (container != null) {
            categoryMap.get(category).add(container);
          } else {
            LOGGER.warn("Null container generated for term {}", term);
          }
        }
      }
    }
    return categoryMap;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
