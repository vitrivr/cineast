package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.StringDoublePair;

/**
 * A {@link SimilarityQueryResultBatch} contains a list of {@link SimilarityQueryResult}s as content of the result message. It combines several results to be posted to the API.
 *
 * @author lucaro
 * @created 12.05.17
 */
public class SimilarityQueryResultBatch {

  /**
   * List of categories to which the content of this similarity query result belong.
   */
  private final List<String> categories;

  /**
   * List of {@link SimilarityQueryResult} that are part of this similarity result batch.
   */
  private final List<SimilarityQueryResult> results;

  /**
   * Constructor for the SimilarityQueryResult object with the results already in the right format of a list of {@link SimilarityQueryResult}.
   *
   * @param categories List of Strings representing the categories of the similarity query results.
   * @param results    List of {@link SimilarityQueryResult} that are results of the query.
   */
  @JsonCreator
  public SimilarityQueryResultBatch(List<String> categories, List<SimilarityQueryResult> results) {
    this.categories = categories;
    this.results = results;
  }

  /**
   * Constructor for the SimilarityQueryResult object with the results in the form of a map mapping categories to results which are filled into the local variables in the right format.
   *
   * @param map     Map of Strings mapped to Lists of {@link StringDoublePair} where categories are mapped to the results of the similarity query.
   * @param queryId String representing the ID of the query to which this part of the result message.
   */
  public SimilarityQueryResultBatch(HashMap<String, List<StringDoublePair>> map, String queryId) {
    this(new ArrayList<>(map.keySet()), new ArrayList<>(map.size()));
    for (String category : this.categories) {
      this.results.add(new SimilarityQueryResult(queryId, category, -1, map.get(category)));
    }
  }

  /**
   * Getter for categories.
   *
   * @return List of String
   */
  public List<String> getCategories() {
    return this.categories;
  }

  /**
   * Getter for results.
   *
   * @return List of {@link SimilarityQueryResult}
   */
  public List<SimilarityQueryResult> getResults() {
    return this.results;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
