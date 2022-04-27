package org.vitrivr.cineast.api.messages.abstracts;

import java.util.List;
import org.vitrivr.cineast.api.messages.result.MediaSegmentMetadataQueryResult;

/**
 * A {@link AbstractQueryResultMessage} represents an abstract Query result to be implemented e.g. a result for a metadata lookup {@link MediaSegmentMetadataQueryResult}.
 */
public abstract class AbstractQueryResultMessage<T> extends AbstractMessage {

  private final Class<T> contentType;
  private final String queryId;
  private final List<T> content;

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

  /**
   * Returns the unique QueryId to which this QueryResultMessage belongs.
   *
   * @return QueryId (unique)
   */
  public String getQueryId() {
    return this.queryId;
  }

  /**
   * Returns a list of the content this QueryResultMessage contains. Has type
   *
   * @return List of type T
   */
  public List<T> getContent() {
    return this.content;
  }

  /**
   * Returns the type of the query result content
   *
   * @return Class instance of type T
   */
  public Class<T> getContentType() {
    return this.contentType;
  }

}
