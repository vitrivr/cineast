package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class QueryTerm {

  /**
   * List of categories defined as part of this {@link QueryTerm}. This ultimately determines the features used for retrieval.
   */
  private final List<String> categories;

  /**
   * Denotes the type of {@link QueryTerm}.
   */
  private final QueryTermType type;

  /**
   * String representation of the query object associated with this query term. Usually base 64 encoded.
   */
  private final String data;

  /**
   * Cached version of the {@link QueryContainer} representation of this {@link QueryTerm}.
   */
  private QueryContainer cachedQueryContainer;

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  /**
   *
   */
  @JsonCreator
  public QueryTerm(@JsonProperty("type") QueryTermType type,
      @JsonProperty("data") String data,
      @JsonProperty("categories") List<String> categories) {
    this.type = type;
    this.categories = categories;
    this.data = data;
  }

  /**
   * Getter for feature categories contained in this {@link QueryTerm}.
   *
   * @return Feature categories contained in this {@link QueryTerm}
   */
  public List<String> getCategories() {
    return this.categories;
  }

  /**
   * Getter for type of this {@link QueryTerm}.
   *
   * @return The {@link QueryTermType} of this {@link QueryTerm}.
   */
  public QueryTermType getType() {
    return type;
  }

  /**
   * Converts the {@link QueryTerm} to a {@link QueryContainer} that can be processed by the retrieval pipeline. This includes conversion of query-objects from the Base64 encoded representation.
   *
   * <strong>IMPORTANT:</strong> Subsequent calls to this method return a cached version of the original {@link QueryContainer}.
   *
   * @return {@link QueryContainer} representation of the {@link QueryTerm}.
   */
  public QueryContainer toContainer() {
    if (this.cachedQueryContainer == null) {
      if (this.data != null) {
        this.cachedQueryContainer = this.type.getQueryContainer(this.data).orElse(null);
      }
    }
    return this.cachedQueryContainer;
  }
}
