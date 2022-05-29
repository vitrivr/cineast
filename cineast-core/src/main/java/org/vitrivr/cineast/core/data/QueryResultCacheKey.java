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
public record QueryResultCacheKey(int queryTermContainerHash, String querySegmentId, String retrieverSpecification, CacheableQueryConfig queryConfig) {

  private QueryResultCacheKey(AbstractQueryTermContainer queryTermContainer, String querySegmentId, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {
    this(queryTermContainer == null ? 0 : queryTermContainer.hashCode(), querySegmentId, retrievers.keySet().stream().map(retriever -> {
      double weight = retrievers.get(retriever);
      return retriever.getClass().getName() + "-" + weight + "|"; //TODO disambiguate between differently configured instances of same retriever
    }).sorted().collect(Collectors.joining()), new CacheableQueryConfig(queryConfig));
  }

  public QueryResultCacheKey(AbstractQueryTermContainer queryTermContainer, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {
    this(queryTermContainer, null, retrievers, queryConfig);
  }

  public QueryResultCacheKey(String querySegmentId, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {
    this(null, querySegmentId, retrievers, queryConfig);
  }

}
