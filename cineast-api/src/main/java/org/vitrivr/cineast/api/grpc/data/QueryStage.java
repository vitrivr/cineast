package org.vitrivr.cineast.api.grpc.data;

import java.util.ArrayList;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

public class QueryStage {

  private final ArrayList<QueryTerm> queryTerms = new ArrayList<>();
  private final ReadableQueryConfig queryConfig;

  public QueryStage(Iterable<QueryTerm> queryTerms, ReadableQueryConfig queryConfig) {
    for (QueryTerm term : queryTerms) {
      this.queryTerms.add(term);
    }
    this.queryConfig = queryConfig;
  }

  public ArrayList<QueryTerm> getQueryTerms() {
    return queryTerms;
  }

  public ReadableQueryConfig getQueryConfig() {
    return queryConfig;
  }

}
