package org.vitrivr.cineast.api.messages.abstracts;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;

/**
 * A {@link AbstractQueryResultMessage} represents an abstract Query result to be implemented i.e. a
 * result for a metadata lookup.
 *
 * @author rgasser
 * @created 22.01.17
 */
public abstract class AbstractQueryResultMessage<T> implements QueryResultMessage<T> {

  /**
   * The content of the query result message of type T.
   */
  private List<T> content;

  /**
   * Class instance of type T to determine content type of the message.
   */
  private final Class<T> contentType;

  /**
   * Query ID of the query to which this represents a result.
   */
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

  /**
   * Getter for queryId.
   *
   * @return String
   */
  @Override
  public String getQueryId() {
    return this.queryId;
  }

  /**
   * Getter for content.
   *
   * @return List of type T
   */
  @Override
  public List<T> getContent() {
    return this.content;
  }

  /**
   * Getter for content type.
   *
   * @return Class instance of type T.
   */
  @Override
  public Class<T> getContentType() {
    return this.contentType;
  }

  /**
   * Get the size of the result content.
   *
   * @return int
   */
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
    return "AbstractQueryResultMessage{" +
        "content=" + content +
        ", queryId='" + queryId + '\'' +
        '}';
  }
}
