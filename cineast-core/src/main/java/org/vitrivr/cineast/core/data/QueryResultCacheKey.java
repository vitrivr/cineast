package org.vitrivr.cineast.core.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

  private final int queryTermContainerHash;
  private final String querySegmentId;
  private final String retrieverSpecification;
  private final CacheableQueryConfig queryConfig;


  private QueryResultCacheKey(AbstractQueryTermContainer queryTermContainer, String querySegmentId, TObjectDoubleHashMap<Retriever> retrievers, ReadableQueryConfig queryConfig) {


    this.querySegmentId = querySegmentId;
    this.queryConfig = new CacheableQueryConfig(queryConfig);
    this.queryTermContainerHash = queryTermContainer == null ? 0 : queryTermContainer.hashCode();

    this.retrieverSpecification = retrievers.keySet().stream().map(retriever -> {
      double weight = retrievers.get(retriever);
      return retriever.getClass().getName() + "-" + weight + "|"; //TODO disambiguate between differently configured instances of same retriever
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
    return queryTermContainerHash == that.queryTermContainerHash
        && Objects.equals(querySegmentId, that.querySegmentId)
        && retrieverSpecification.equals(that.retrieverSpecification)
        && queryConfig.equals(that.queryConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryTermContainerHash, querySegmentId, retrieverSpecification, queryConfig);
  }
}
