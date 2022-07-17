package org.vitrivr.cineast.api.messages.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.vitrivr.cineast.core.data.StringDoublePair;

/**
 * A {@link SimilarityQueryResultBatch} contains a list of {@link SimilarityQueryResult}s as content of the result message. It combines several results to be posted to the API.
 *
 * @param categories List of Strings representing the categories of the similarity query results.
 * @param results    List of {@link SimilarityQueryResult} that are results of the query.
 */
public record SimilarityQueryResultBatch(List<String> categories, List<SimilarityQueryResult> results) {

  /**
   * Constructor for the SimilarityQueryResult object with the results in the form of a map mapping categories to results which are filled into the local variables in the right format.
   *
   * @param map     Map of Strings mapped to Lists of {@link StringDoublePair} where categories are mapped to the results of the similarity query.
   * @param queryId String representing the ID of the query to which this part of the result message.
   */
  public SimilarityQueryResultBatch(HashMap<String, List<StringDoublePair>> map, String queryId) {
    this(new ArrayList<>(map.keySet()), new ArrayList<>(map.size()));
    for (String category : this.categories()) {
      this.results.add(new SimilarityQueryResult(queryId, map.get(category), category, -1));
    }
  }

}
