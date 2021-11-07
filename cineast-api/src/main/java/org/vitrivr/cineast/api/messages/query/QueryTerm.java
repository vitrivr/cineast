package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;

/**
 * Contains the data of a particular {@link QueryTerm}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
   * Cached version of the {@link AbstractQueryTermContainer} representation of this {@link QueryTerm}.
   */
  private AbstractQueryTermContainer cachedQueryTermContainer;

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }

  /**
   * Constructor for the QueryTerm object.
   *
   * @param type       The {@link QueryTermType} of the {@link QueryTerm}.
   * @param data       The actual data of the {@link QueryTerm}
   * @param categories List of categories of the {@link QueryTerm}
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
   * Converts the {@link QueryTerm} to a {@link AbstractQueryTermContainer} that can be processed by the retrieval pipeline. This includes conversion of query-objects from the Base64 encoded representation.
   *
   * <strong>IMPORTANT:</strong> Subsequent calls to this method return a cached version of the original {@link AbstractQueryTermContainer}.
   *
   * @return {@link AbstractQueryTermContainer} representation of the {@link QueryTerm}.
   */
  public AbstractQueryTermContainer toContainer() {
    if (this.cachedQueryTermContainer == null) {
      if (this.data != null) {
        this.cachedQueryTermContainer = this.type.getQueryContainer(this.data).orElse(null);
      }
    }
    return this.cachedQueryTermContainer;
  }
}
