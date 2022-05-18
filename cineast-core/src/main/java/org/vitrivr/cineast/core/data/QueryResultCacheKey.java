package org.vitrivr.cineast.core.data;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import org.vitrivr.cineast.core.config.CacheableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.features.retriever.Retriever;

/**
 * A container to serve as a key for caching query results
 */
public class QueryResultCacheKey {

  private final AbstractQueryTermContainer queryTermContainer;
  private final String querySegmentId;
  private final String retrieverSpecification;
  private final CacheableQueryConfig queryConfig;

  private QueryResultCacheKey(AbstractQueryTermContainer queryTermContainer, String querySegmentId, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {

    this.queryTermContainer = queryTermContainer;
    this.querySegmentId = querySegmentId;
    this.queryConfig = new CacheableQueryConfig(queryConfig);

    this.retrieverSpecification = retrievers.keySet().stream().map(retriever -> {
      double weight = retrievers.get(retriever);

      return retriever.getClass().getName() + "-" + System.identityHashCode(retriever) + "-" + weight + "|";
    }).sorted().collect(Collectors.joining());

  }

  public QueryResultCacheKey(AbstractQueryTermContainer queryTermContainer, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {
    this(queryTermContainer, null, retrievers, queryConfig);
  }

  public QueryResultCacheKey(String querySegmentId, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {
    this(null, querySegmentId, retrievers, queryConfig);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryResultCacheKey that = (QueryResultCacheKey) o;
    return Objects.equals(queryTermContainer, that.queryTermContainer) && Objects.equals(querySegmentId, that.querySegmentId) && retrieverSpecification.equals(that.retrieverSpecification) && queryConfig.equals(that.queryConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryTermContainer, querySegmentId, retrieverSpecification, queryConfig);
  }
}
