package org.vitrivr.cineast.api.messages.query;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

/**
 * A {@link Query} represents an abstract Query to be implemented i.e. a request for a similarity-search.
 */
public abstract class Query extends AbstractMessage {

  /**
   * The {@link QueryConfig} that should be used to configure the query. May be null!
   */
  protected final QueryConfig config;

  /**
   * Constructor for the Query object.
   *
   * @param config The {@link ReadableQueryConfig}. May be null!
   */
  public Query(QueryConfig config) {
    this.config = config;
  }

  /**
   * JSON: the config field is specified in subclasses.
   *
   * <p>The JsonIgnore is needed because the actual field is not called queryconfig</p>
   */
  @JsonIgnore
  public final QueryConfig getQueryConfig() {
    return this.config;
  }

}
