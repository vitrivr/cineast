package org.vitrivr.cineast.api.messages.abstracts;

import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.api.messages.result.MediaSegmentMetadataQueryResult;

/**
 * A {@link AbstractQueryResultMessage} represents an abstract Query result to be implemented e.g. a result for a metadata lookup {@link MediaSegmentMetadataQueryResult}.
 *
 * @author rgasser
 * @created 22.01.17
 */
public abstract class AbstractQueryResultMessage<T> implements QueryResultMessage<T> {

  private List<T> content;

  private final Class<T> contentType;

  private final String queryId;

  /**
   * Constructor for the AbstractQueryResultMessage object.
   *
   * @param queryId     The query ID of the query corresponding to this result.
   * @param contentType Content type of the result.
   * @param content     Result of the query.
   */
  public AbstractQueryResultMessage(String queryId, Class<T> contentType, List<T> content) {
    this.queryId = queryId;
    this.contentType = contentType;
    this.content = content;
  }

  @Override
  public String getQueryId() {
    return this.queryId;
  }

  @Override
  public List<T> getContent() {
    return this.content;
  }

  @Override
  public Class<T> getContentType() {
    return this.contentType;
  }

  @Override
  public int count() {
    if (this.content != null) {
      return this.content.size();
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
