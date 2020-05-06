package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.vitrivr.cineast.core.data.StringDoublePair;

public class SimilarityQueryResultBatch {

  private List<String> categories;
  private List<SimilarityQueryResult> results;

  @JsonCreator
  public SimilarityQueryResultBatch(List<String> categories, List<SimilarityQueryResult> results) {
    this.categories = categories;
    this.results = results;
  }

  public SimilarityQueryResultBatch(HashMap<String, List<StringDoublePair>> map, String queryId) {
    this(new ArrayList<>(map.keySet()), new ArrayList<>(map.size()));
    for (String category : this.categories) {
      this.results.add(new SimilarityQueryResult(queryId, category, -1, map.get(category)));
    }
  }

  public List<String> getCategories() {
    return this.categories;
  }

  public List<SimilarityQueryResult> getResults() {
    return this.results;
  }

  @Override
  public String toString() {
    return "SimilarityQueryResultBatch{" +
        "categories=" + categories +
        ", results=" + results +
        '}';
  }
}
